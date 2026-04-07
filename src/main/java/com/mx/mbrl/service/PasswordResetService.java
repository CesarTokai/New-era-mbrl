package com.mx.mbrl.service;

import com.mx.mbrl.entity.PasswordHistory;
import com.mx.mbrl.entity.PasswordResetToken;
import com.mx.mbrl.entity.User;
import com.mx.mbrl.repository.PasswordHistoryRepository;
import com.mx.mbrl.repository.PasswordResetTokenRepository;
import com.mx.mbrl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final UserRepository userRepository;
	private final PasswordHistoryRepository passwordHistoryRepository;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;

	private static final int RESET_TOKEN_EXPIRATION_HOURS = 24;

	@Transactional
	public void generateResetToken(String email) {
		log.info("Generando token de reset para email: {}", email);

		// Buscar usuario
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> {
					log.warn("Usuario no encontrado con email: {}", email);
					return new IllegalArgumentException("Usuario no encontrado");
				});

		// Revocar tokens previos activos
		passwordResetTokenRepository.findByUserIdAndUsedFalse(user.getId())
				.ifPresent(token -> {
					token.setUsed(true);
					token.setUsedAt(LocalDateTime.now());
					passwordResetTokenRepository.save(token);
					log.debug("Token anterior revocado para usuario ID: {}", user.getId());
				});

		// Generar nuevo token
		PasswordResetToken resetToken = new PasswordResetToken();
		resetToken.setUser(user);
		resetToken.setToken(UUID.randomUUID().toString());
		resetToken.setExpiryDate(LocalDateTime.now().plusHours(RESET_TOKEN_EXPIRATION_HOURS));
		resetToken.setUsed(false);
		resetToken.setCreatedAt(LocalDateTime.now());

		PasswordResetToken savedToken = passwordResetTokenRepository.save(resetToken);
		log.info("Token de reset generado para usuario ID: {}", user.getId());

		// Enviar email
		try {
			emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), savedToken.getToken());
		} catch (Exception e) {
			log.error("Error enviando email de reset: {}", e.getMessage());
			// No fallar si el email falla - el token sigue siendo válido
		}
	}

	@Transactional(readOnly = true)
	public boolean validateResetToken(String token) {
		log.debug("Validando token de reset");

		PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
				.orElseThrow(() -> {
					log.warn("Token de reset inválido");
					return new IllegalArgumentException("Token de reset inválido o expirado");
				});

		if (resetToken.getUsed()) {
			log.warn("Token ya fue utilizado");
			throw new IllegalArgumentException("Token ya fue utilizado");
		}

		if (resetToken.isExpired()) {
			log.warn("Token de reset expirado");
			throw new IllegalArgumentException("Token de reset ha expirado");
		}

		log.debug("Token de reset válido");
		return true;
	}

	@Transactional
	public void resetPassword(String token, String newPassword, String confirmPassword) {
		log.info("Reset de contraseña con token");

		// Validar que las contraseñas coincidan
		if (!newPassword.equals(confirmPassword)) {
			throw new IllegalArgumentException("Las contraseñas no coinciden");
		}

		// Validar token
		PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("Token de reset inválido"));

		if (resetToken.getUsed()) {
			throw new IllegalArgumentException("Token ya fue utilizado");
		}

		if (resetToken.isExpired()) {
			throw new IllegalArgumentException("Token de reset ha expirado");
		}

		// Obtener usuario
		User user = resetToken.getUser();

		// Validar que nueva contraseña sea diferente
		if (passwordEncoder.matches(newPassword, user.getPassword())) {
			throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la actual");
		}

		// Registrar cambio en historial
		PasswordHistory history = new PasswordHistory();
		history.setUser(user);
		history.setOldPasswordHash(user.getPassword());
		history.setNewPasswordHash(passwordEncoder.encode(newPassword));
		history.setChangedAt(LocalDateTime.now());
		history.setReason("PASSWORD_RESET");

		passwordHistoryRepository.save(history);
		log.debug("Cambio de contraseña registrado en historial para usuario ID: {}", user.getId());

		// Actualizar contraseña
		user.setPassword(passwordEncoder.encode(newPassword));
		user.setLastPasswordChangeDate(LocalDateTime.now());
		userRepository.save(user);

		// Marcar token como utilizado
		resetToken.setUsed(true);
		resetToken.setUsedAt(LocalDateTime.now());
		passwordResetTokenRepository.save(resetToken);

		log.info("Contraseña reseteada exitosamente para usuario ID: {}", user.getId());

		// Enviar notificación
		try {
			emailService.sendPasswordChangeNotification(user.getEmail(), user.getUsername());
		} catch (Exception e) {
			log.error("Error enviando notificación: {}", e.getMessage());
		}
	}

	@Scheduled(fixedDelay = 3600000) // Cada hora
	@Transactional
	public void cleanExpiredResetTokens() {
		log.info("Limpiando tokens de reset expirados");

		try {
			LocalDateTime now = LocalDateTime.now();
			passwordResetTokenRepository.deleteByExpiryDateBefore(now);
			log.debug("Tokens de reset expirados removidos");
		} catch (Exception e) {
			log.error("Error durante limpieza de tokens expirados: {}", e.getMessage());
		}
	}
}


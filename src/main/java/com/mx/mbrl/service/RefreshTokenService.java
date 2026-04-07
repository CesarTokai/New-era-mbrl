package com.mx.mbrl.service;

import com.mx.mbrl.entity.RefreshToken;
import com.mx.mbrl.entity.User;
import com.mx.mbrl.repository.RefreshTokenRepository;
import com.mx.mbrl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;

	@Value("${jwt.refresh-expiration:604800000}") // 7 días por defecto
	private long refreshTokenExpirationMs;

	@Transactional
	public RefreshToken createRefreshToken(Long userId) {
		log.info("Creando refresh token para usuario ID: {}", userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

		// Revocar tokens previos si existen
		refreshTokenRepository.findByUserIdAndRevokedFalse(userId)
				.forEach(token -> {
					token.setRevoked(true);
					refreshTokenRepository.save(token);
				});

		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(user);
		refreshToken.setToken(UUID.randomUUID().toString());
		refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000));
		refreshToken.setRevoked(false);
		refreshToken.setCreatedAt(LocalDateTime.now());

		RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
		log.info("Refresh token creado para usuario ID: {}", userId);

		return savedToken;
	}

	@Transactional(readOnly = true)
	public RefreshToken validateRefreshToken(String token) {
		log.debug("Validando refresh token");

		RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
				.orElseThrow(() -> {
					log.warn("Refresh token inválido: {}", token);
					return new IllegalArgumentException("Refresh token inválido");
				});

		if (refreshToken.isExpired()) {
			log.warn("Refresh token expirado para usuario ID: {}", refreshToken.getUser().getId());
			throw new IllegalArgumentException("Refresh token ha expirado");
		}

		if (refreshToken.getRevoked()) {
			log.warn("Refresh token revocado para usuario ID: {}", refreshToken.getUser().getId());
			throw new IllegalArgumentException("Refresh token ha sido revocado");
		}

		log.debug("Refresh token válido para usuario ID: {}", refreshToken.getUser().getId());
		return refreshToken;
	}

	@Transactional
	public void revokeRefreshToken(String token) {
		log.info("Revocando refresh token");

		RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("Refresh token no encontrado"));

		refreshToken.setRevoked(true);
		refreshTokenRepository.save(refreshToken);

		log.info("Refresh token revocado para usuario ID: {}", refreshToken.getUser().getId());
	}

	@Transactional
	public void revokeAllUserTokens(Long userId) {
		log.info("Revocando todos los refresh tokens del usuario ID: {}", userId);

		refreshTokenRepository.findByUserIdAndRevokedFalse(userId)
				.forEach(token -> {
					token.setRevoked(true);
					refreshTokenRepository.save(token);
				});

		log.info("Todos los refresh tokens revocados para usuario ID: {}", userId);
	}

	@Transactional
	public void deleteExpiredTokens() {
		log.debug("Eliminando refresh tokens expirados");

		refreshTokenRepository.findAll().stream()
				.filter(RefreshToken::isExpired)
				.forEach(token -> refreshTokenRepository.deleteById(token.getId()));

		log.debug("Refresh tokens expirados eliminados");
	}
}


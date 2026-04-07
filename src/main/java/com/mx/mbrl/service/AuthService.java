package com.mx.mbrl.service;

import com.mx.mbrl.dto.ChangePasswordRequestDTO;
import com.mx.mbrl.dto.CustomerRequestDTO;
import com.mx.mbrl.dto.JwtResponse;
import com.mx.mbrl.dto.LoginRequestDTO;
import com.mx.mbrl.entity.Customer;
import com.mx.mbrl.entity.PasswordHistory;
import com.mx.mbrl.entity.RefreshToken;
import com.mx.mbrl.entity.User;
import com.mx.mbrl.repository.CustomerRepository;
import com.mx.mbrl.repository.PasswordHistoryRepository;
import com.mx.mbrl.repository.UserRepository;
import com.mx.mbrl.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final CustomerRepository customerRepository;
	private final PasswordHistoryRepository passwordHistoryRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;
	private final TokenBlacklistService tokenBlacklistService;

	private static final int PASSWORD_CHANGE_DAYS = 90;

	@Transactional
	public Customer register(CustomerRequestDTO customerRequestDTO) {
		log.info("Registrando nuevo cliente: {}", customerRequestDTO.getName());

		// Validar que el email no exista
		if (userRepository.findByEmail(customerRequestDTO.getEmail()).isPresent()) {
			throw new IllegalArgumentException("El email ya está registrado");
		}

		// Crear usuario
		User user = new User();
		user.setUsername(customerRequestDTO.getEmail().split("@")[0]);
		user.setEmail(customerRequestDTO.getEmail());
		user.setPassword(passwordEncoder.encode("defaultPassword123"));
		user.setRole(User.Role.USER);
		
		User savedUser = userRepository.save(user);
		log.info("Usuario creado con ID: {}", savedUser.getId());

		// Crear cliente
		Customer customer = new Customer();
		customer.setUser(savedUser);
		customer.setName(customerRequestDTO.getName());
		customer.setEmail(customerRequestDTO.getEmail());
		customer.setPhone(customerRequestDTO.getPhone());
		customer.setAddress(customerRequestDTO.getAddress());
		customer.setCity(customerRequestDTO.getCity());
		customer.setState(customerRequestDTO.getState());
		customer.setPostalCode(customerRequestDTO.getPostalCode());

		Customer savedCustomer = customerRepository.save(customer);
		log.info("Cliente creado con ID: {}", savedCustomer.getId());

		return savedCustomer;
	}

	@Transactional
	public JwtResponse login(LoginRequestDTO loginRequestDTO) {
		log.info("Login para usuario: {}", loginRequestDTO.getEmail());

		// Buscar usuario por email
		User user = userRepository.findByEmail(loginRequestDTO.getEmail())
				.orElseThrow(() -> new IllegalArgumentException("Email o contraseña incorrectos"));

		// Validar contraseña
		if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
			log.warn("Intento de login fallido para email: {}", loginRequestDTO.getEmail());
			throw new IllegalArgumentException("Email o contraseña incorrectos");
		}

		// Generar JWT access token
		String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

		// Generar refresh token
		RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

		log.info("Login exitoso para usuario: {}", user.getEmail());

		// Construir respuesta
		JwtResponse response = new JwtResponse();
		response.setAccessToken(accessToken);
		response.setRefreshToken(refreshToken.getToken());
		response.setType("Bearer");
		response.setId(user.getId());
		response.setUsername(user.getUsername());
		response.setEmail(user.getEmail());
		response.setRole(user.getRole().name());
		response.setExpiresIn(86400000L); // 24 horas

		return response;
	}

	@Transactional
	public JwtResponse refreshAccessToken(String refreshToken) {
		log.info("Refrescando access token");

		// Validar refresh token
		RefreshToken validRefreshToken = refreshTokenService.validateRefreshToken(refreshToken);
		User user = validRefreshToken.getUser();

		// Generar nuevo access token
		String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

		log.info("Access token refrescado para usuario: {}", user.getEmail());

		// Construir respuesta
		JwtResponse response = new JwtResponse();
		response.setAccessToken(newAccessToken);
		response.setRefreshToken(refreshToken); // Reutilizar mismo refresh token
		response.setType("Bearer");
		response.setId(user.getId());
		response.setUsername(user.getUsername());
		response.setEmail(user.getEmail());
		response.setRole(user.getRole().name());
		response.setExpiresIn(86400000L); // 24 horas

		return response;
	}

	@Transactional
	public void logout(String accessToken, String refreshToken) {
		log.info("Logout de usuario");

		try {
			// Invalidar access token agregándolo a blacklist
			if (accessToken != null && !accessToken.isEmpty()) {
				tokenBlacklistService.invalidateToken(accessToken);
				log.debug("Access token agregado a blacklist");
			}

			// Revocar refresh token
			if (refreshToken != null && !refreshToken.isEmpty()) {
				refreshTokenService.revokeRefreshToken(refreshToken);
				log.debug("Refresh token revocado");
			}

			log.info("Logout exitoso");
		} catch (Exception e) {
			log.warn("Error durante logout: {}", e.getMessage());
			// No lanzar excepción - permitir logout aunque falle el revoke
		}
	}

	@Transactional
	public void changePassword(Long userId, ChangePasswordRequestDTO changePasswordRequest) {
		log.info("Cambiando contraseña para usuario ID: {}", userId);

		// Validar que las contraseñas nuevas coincidan
		if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
			throw new IllegalArgumentException("Las contraseñas no coinciden");
		}

		// Validar que nueva contraseña sea diferente a la antigua
		if (changePasswordRequest.getOldPassword().equals(changePasswordRequest.getNewPassword())) {
			throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la actual");
		}

		// Buscar usuario
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		// Validar contraseña actual
		if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
			log.warn("Intento fallido de cambio de contraseña para usuario ID: {}", userId);
			throw new IllegalArgumentException("La contraseña actual es incorrecta");
		}

		// Registrar cambio en historial
		PasswordHistory history = new PasswordHistory();
		history.setUser(user);
		history.setOldPasswordHash(user.getPassword());
		history.setNewPasswordHash(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
		history.setChangedAt(LocalDateTime.now());
		history.setReason("USER_REQUEST");

		passwordHistoryRepository.save(history);
		log.debug("Cambio de contraseña registrado en historial para usuario ID: {}", userId);

		// Actualizar contraseña del usuario
		String newHashedPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());
		user.setPassword(newHashedPassword);
		user.setLastPasswordChangeDate(LocalDateTime.now());

		userRepository.save(user);
		log.info("Contraseña actualizada exitosamente para usuario ID: {}", userId);
	}

	@Transactional(readOnly = true)
	public boolean isPasswordChangeRequired(Long userId) {
		log.debug("Verificando si cambio de contraseña es requerido para usuario ID: {}", userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		boolean changeRequired = user.isPasswordChangeRequired();
		log.debug("Cambio de contraseña requerido para usuario ID: {} = {}", userId, changeRequired);

		return changeRequired;
	}

	@Transactional(readOnly = true)
	public int getDaysUntilPasswordExpiration(Long userId) {
		log.debug("Calculando días hasta expiración de contraseña para usuario ID: {}", userId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		if (user.getLastPasswordChangeDate() == null) {
			return 0; // Contraseña ya expiró
		}

		LocalDateTime expirationDate = user.getLastPasswordChangeDate().plusDays(PASSWORD_CHANGE_DAYS);
		LocalDateTime now = LocalDateTime.now();

		if (now.isAfter(expirationDate)) {
			return 0; // Ya expiró
		}

		long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(now, expirationDate);
		return (int) daysRemaining;
	}
}


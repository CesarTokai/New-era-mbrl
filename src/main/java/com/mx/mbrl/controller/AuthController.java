package com.mx.mbrl.controller;

import com.mx.mbrl.dto.*;
import com.mx.mbrl.entity.Customer;
import com.mx.mbrl.entity.User;
import com.mx.mbrl.repository.UserRepository;
import com.mx.mbrl.service.AuthService;
import com.mx.mbrl.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final PasswordResetService passwordResetService;
	private final UserRepository userRepository;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<Customer>> register(@Valid @RequestBody CustomerRequestDTO dto) {
		log.info("Registrando nuevo cliente: {}", dto.getName());

		try {
			Customer customer = authService.register(dto);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(customer, "Cliente registrado exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error en registro: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		}
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequestDTO dto) {
		log.info("Login para usuario: {}", dto.getEmail());

		try {
			JwtResponse response = authService.login(dto);
			return ResponseEntity.ok(ApiResponse.success(response, "Login exitoso"));
		} catch (IllegalArgumentException e) {
			log.error("Error en login: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.error(e.getMessage(), 401));
		}
	}

	/**
	 * Renueva el access token usando el refresh token.
	 * El refreshToken se envía en el body como JSON: { "refreshToken": "..." }
	 * o como @RequestParam para compatibilidad con clientes existentes.
	 */
	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@RequestParam String refreshToken) {
		log.info("Refrescando access token");

		try {
			JwtResponse response = authService.refreshAccessToken(refreshToken);
			return ResponseEntity.ok(ApiResponse.success(response, "Token refrescado exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error refrescando token: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.error(e.getMessage(), 401));
		}
	}

	/**
	 * Cierra la sesión del usuario.
	 * - El access token se lee automáticamente del header Authorization (no hace falta enviarlo aparte).
	 * - El refresh token se envía como @RequestParam (es un UUID simple, no un JWT).
	 *
	 * Ejemplo de llamada:
	 *   POST /api/auth/logout?refreshToken=uuid-aqui
	 *   Authorization: Bearer <access_token>
	 */
	@PostMapping("/logout")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<Void>> logout(
			HttpServletRequest request,
			@RequestParam(required = false) String refreshToken) {

		// Extraer el access token del header Authorization (ya viene en la petición)
		String accessToken = extractBearerToken(request);

		log.info("Logout de usuario");

		try {
			authService.logout(accessToken, refreshToken);
			return ResponseEntity.ok(ApiResponse.success(null, "Logout exitoso"));
		} catch (Exception e) {
			log.error("Error en logout: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		}
	}

	/**
	 * Cambia la contraseña del usuario autenticado.
	 * El userId se obtiene del JWT — no hace falta enviarlo.
	 *
	 * Body: { "oldPassword": "...", "newPassword": "...", "confirmPassword": "..." }
	 */
	@PostMapping("/change-password")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<Void>> changePassword(
			Authentication authentication,
			@Valid @RequestBody ChangePasswordRequestDTO dto) {

		// Obtener userId desde el JWT (el email está en authentication.getName())
		Long userId = getUserIdFromAuth(authentication);
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.error("No se pudo identificar al usuario autenticado", 401));
		}

		log.info("Cambiando contraseña para usuario ID: {}", userId);

		try {
			authService.changePassword(userId, dto);
			return ResponseEntity.ok(ApiResponse.success(null, "Contraseña actualizada exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error cambiando contraseña: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		}
	}

	/**
	 * Retorna el estado de expiración de contraseña del usuario autenticado.
	 * No requiere pasar userId — se obtiene del JWT.
	 */
	@GetMapping("/password-status")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<PasswordStatusDTO>> getPasswordStatus(Authentication authentication) {

		Long userId = getUserIdFromAuth(authentication);
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.error("No se pudo identificar al usuario autenticado", 401));
		}

		log.info("Verificando estado de contraseña para usuario ID: {}", userId);

		try {
			boolean changeRequired = authService.isPasswordChangeRequired(userId);
			int daysRemaining = authService.getDaysUntilPasswordExpiration(userId);

			PasswordStatusDTO statusDTO = new PasswordStatusDTO();
			statusDTO.setChangeRequired(changeRequired);
			statusDTO.setDaysRemaining(daysRemaining);
			statusDTO.setExpirationDays(90);

			return ResponseEntity.ok(ApiResponse.success(statusDTO, "Estado de contraseña obtenido"));
		} catch (IllegalArgumentException e) {
			log.error("Error obteniendo estado: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		}
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
		log.info("Solicitud de reset de contraseña para email: {}", email);

		try {
			passwordResetService.generateResetToken(email);
		} catch (Exception ignored) {
			// No revelar si el email existe (security best practice)
		}
		return ResponseEntity.ok(ApiResponse.success(null,
				"Si el email existe, recibirás instrucciones para resetear tu contraseña"));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO dto) {
		log.info("Reset de contraseña con token");

		try {
			passwordResetService.resetPassword(dto.getToken(), dto.getNewPassword(), dto.getConfirmPassword());
			return ResponseEntity.ok(ApiResponse.success(null, "Contraseña reseteada exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error en reset de contraseña: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		}
	}

	@GetMapping("/validate-reset-token")
	public ResponseEntity<ApiResponse<Boolean>> validateResetToken(@RequestParam String token) {
		log.info("Validando token de reset");

		try {
			boolean isValid = passwordResetService.validateResetToken(token);
			return ResponseEntity.ok(ApiResponse.success(isValid, "Token válido"));
		} catch (IllegalArgumentException e) {
			log.debug("Token inválido o expirado: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		}
	}

	// ── Helpers privados ──────────────────────────────────────────────────────

	/** Extrae el Bearer token del header Authorization. */
	private String extractBearerToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
			return header.substring(7);
		}
		return null;
	}

	/** Obtiene el userId a partir del email almacenado en el JWT (Authentication.getName()). */
	private Long getUserIdFromAuth(Authentication authentication) {
		if (authentication == null || !StringUtils.hasText(authentication.getName())) {
			return null;
		}
		return userRepository.findByEmail(authentication.getName())
				.map(User::getId)
				.orElse(null);
	}
}

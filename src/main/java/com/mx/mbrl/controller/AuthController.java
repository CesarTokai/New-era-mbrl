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

	// POST /api/auth/register  — público
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

	/**
	 * POST /api/auth/login — público
	 * Body: { "email": "...", "password": "..." }
	 * Respuesta: { "data": { "accessToken": "...", "type": "Bearer", ... } }
	 * Usa el accessToken en el header: Authorization: Bearer <accessToken>
	 */
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
	 * POST /api/auth/logout — requiere JWT
	 * Header: Authorization: Bearer <accessToken>
	 * El cliente descarta el token localmente; el token expira según jwt.expiration (24h).
	 */
	@PostMapping("/logout")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
		String accessToken = extractBearerToken(request);
		log.info("Logout de usuario");
		authService.logout(accessToken, null);
		return ResponseEntity.ok(ApiResponse.success(null, "Logout exitoso"));
	}

	/**
	 * POST /api/auth/change-password — requiere JWT
	 * Body: { "oldPassword": "...", "newPassword": "...", "confirmPassword": "..." }
	 */
	@PostMapping("/change-password")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<Void>> changePassword(
			Authentication authentication,
			@Valid @RequestBody ChangePasswordRequestDTO dto) {

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

	// POST /api/auth/forgot-password?email=...  — público
	@PostMapping("/forgot-password")
	public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
		log.info("Solicitud de reset de contraseña para email: {}", email);
		try {
			passwordResetService.generateResetToken(email);
		} catch (Exception ignored) {
			// No revelar si el email existe
		}
		return ResponseEntity.ok(ApiResponse.success(null,
				"Si el email existe, recibirás instrucciones para resetear tu contraseña"));
	}

	// POST /api/auth/reset-password  — público
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

	// GET /api/auth/validate-reset-token?token=...  — público
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

	// ── Helpers ─────────────────────────────────────────────────────────────

	private String extractBearerToken(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
			return header.substring(7);
		}
		return null;
	}

	private Long getUserIdFromAuth(Authentication authentication) {
		if (authentication == null || !StringUtils.hasText(authentication.getName())) {
			return null;
		}
		return userRepository.findByEmail(authentication.getName())
				.map(User::getId)
				.orElse(null);
	}
}

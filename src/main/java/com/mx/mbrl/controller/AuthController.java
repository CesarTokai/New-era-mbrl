package com.mx.mbrl.controller;

import com.mx.mbrl.dto.*;
import com.mx.mbrl.entity.Customer;
import com.mx.mbrl.service.AuthService;
import com.mx.mbrl.service.CustomerService;
import com.mx.mbrl.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final CustomerService customerService;
	private final PasswordResetService passwordResetService;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<Customer>> register(@Valid @RequestBody CustomerRequestDTO customerRequestDTO) {
		log.info("Registrando nuevo cliente: {}", customerRequestDTO.getName());

		try {
			Customer customer = authService.register(customerRequestDTO);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(customer, "Cliente registrado exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error en registro: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		}
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
		log.info("Login para usuario: {}", loginRequestDTO.getEmail());

		try {
			JwtResponse response = authService.login(loginRequestDTO);
			return ResponseEntity.ok(ApiResponse.success(response, "Login exitoso"));
		} catch (IllegalArgumentException e) {
			log.error("Error en login: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(ApiResponse.error(e.getMessage(), 401));
		}
	}

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

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<Void>> logout(
			@RequestParam(required = false) String accessToken,
			@RequestParam(required = false) String refreshToken) {
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

	@PostMapping("/change-password")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<Void>> changePassword(
			@RequestParam Long userId,
			@Valid @RequestBody ChangePasswordRequestDTO changePasswordRequest) {
		log.info("Cambiando contraseña para usuario ID: {}", userId);

		try {
			authService.changePassword(userId, changePasswordRequest);
			return ResponseEntity.ok(ApiResponse.success(null, "Contraseña actualizada exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error cambiando contraseña: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		}
	}

	@GetMapping("/password-status/{userId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ApiResponse<PasswordStatusDTO>> getPasswordStatus(@PathVariable Long userId) {
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
			// No revelar si el email existe o no (security best practice)
			return ResponseEntity.ok(ApiResponse.success(null, "Si el email existe, recibirás instrucciones para resetear tu contraseña"));
		} catch (Exception e) {
			log.error("Error en forgot password: {}", e.getMessage());
			// Retornar mensaje genérico
			return ResponseEntity.ok(ApiResponse.success(null, "Si el email existe, recibirás instrucciones para resetear tu contraseña"));
		}
	}

	@PostMapping("/reset-password")
	public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO resetPasswordRequest) {
		log.info("Reset de contraseña con token");

		try {
			passwordResetService.resetPassword(
					resetPasswordRequest.getToken(),
					resetPasswordRequest.getNewPassword(),
					resetPasswordRequest.getConfirmPassword()
			);
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
}



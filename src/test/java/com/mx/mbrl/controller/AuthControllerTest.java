package com.mx.mbrl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.mbrl.dto.*;
import com.mx.mbrl.entity.Customer;
import com.mx.mbrl.service.AuthService;
import com.mx.mbrl.service.CustomerService;
import com.mx.mbrl.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController - Pruebas Unitarias")
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthService authService;

	@MockBean
	private CustomerService customerService;

	@MockBean
	private PasswordResetService passwordResetService;

	private CustomerRequestDTO customerRequestDTO;
	private LoginRequestDTO loginRequestDTO;
	private Customer testCustomer;
	private JwtResponse jwtResponse;

	@BeforeEach
	void setUp() {
		// Configurar datos de prueba
		customerRequestDTO = new CustomerRequestDTO();
		customerRequestDTO.setName("Juan Pérez");
		customerRequestDTO.setEmail("juan@example.com");
		customerRequestDTO.setPhone("1234567890");
		customerRequestDTO.setAddress("Calle Principal 123");
		customerRequestDTO.setCity("Ciudad");
		customerRequestDTO.setState("Estado");
		customerRequestDTO.setPostalCode("12345");

		loginRequestDTO = new LoginRequestDTO();
		loginRequestDTO.setEmail("juan@example.com");
		loginRequestDTO.setPassword("password123");

		testCustomer = new Customer();
		testCustomer.setId(1L);
		testCustomer.setName("Juan Pérez");
		testCustomer.setEmail("juan@example.com");

		jwtResponse = new JwtResponse();
		jwtResponse.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
		jwtResponse.setRefreshToken("refresh_token_123");
		jwtResponse.setType("Bearer");
		jwtResponse.setId(1L);
		jwtResponse.setUsername("juan");
		jwtResponse.setEmail("juan@example.com");
		jwtResponse.setRole("USER");
	}

	// ==================== POST /api/auth/register ====================
	@Test
	@DisplayName("Registro exitoso de nuevo cliente")
	void testRegisterSuccess() throws Exception {
		when(authService.register(any(CustomerRequestDTO.class))).thenReturn(testCustomer);

		mockMvc.perform(post("/api/auth/register")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(customerRequestDTO)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Cliente registrado exitosamente"))
				.andExpect(jsonPath("$.data.id").value(1))
				.andExpect(jsonPath("$.data.name").value("Juan Pérez"));

		verify(authService, times(1)).register(any(CustomerRequestDTO.class));
	}

	@Test
	@DisplayName("Registro falla por email duplicado")
	void testRegisterFailureDuplicateEmail() throws Exception {
		when(authService.register(any(CustomerRequestDTO.class)))
				.thenThrow(new IllegalArgumentException("El email ya está registrado"));

		mockMvc.perform(post("/api/auth/register")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(customerRequestDTO)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("El email ya está registrado"));

		verify(authService, times(1)).register(any(CustomerRequestDTO.class));
	}

	@Test
	@DisplayName("Registro falla por validación de campo vacío")
	void testRegisterFailureValidation() throws Exception {
		customerRequestDTO.setEmail(""); // Email vacío

		mockMvc.perform(post("/api/auth/register")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(customerRequestDTO)))
				.andExpect(status().isBadRequest());
	}

	// ==================== POST /api/auth/login ====================
	@Test
	@DisplayName("Login exitoso")
	void testLoginSuccess() throws Exception {
		when(authService.login(any(LoginRequestDTO.class))).thenReturn(jwtResponse);

		mockMvc.perform(post("/api/auth/login")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequestDTO)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Login exitoso"))
				.andExpect(jsonPath("$.data.token").exists())
				.andExpect(jsonPath("$.data.type").value("Bearer"))
				.andExpect(jsonPath("$.data.email").value("juan@example.com"));

		verify(authService, times(1)).login(any(LoginRequestDTO.class));
	}

	@Test
	@DisplayName("Login falla por credenciales inválidas")
	void testLoginFailureInvalidCredentials() throws Exception {
		when(authService.login(any(LoginRequestDTO.class)))
				.thenThrow(new IllegalArgumentException("Email o contraseña inválidos"));

		mockMvc.perform(post("/api/auth/login")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequestDTO)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Email o contraseña inválidos"));

		verify(authService, times(1)).login(any(LoginRequestDTO.class));
	}

	@Test
	@DisplayName("Login falla por usuario no encontrado")
	void testLoginFailureUserNotFound() throws Exception {
		when(authService.login(any(LoginRequestDTO.class)))
				.thenThrow(new IllegalArgumentException("Usuario no encontrado"));

		mockMvc.perform(post("/api/auth/login")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequestDTO)))
				.andExpect(status().isUnauthorized());

		verify(authService, times(1)).login(any(LoginRequestDTO.class));
	}

	// ==================== POST /api/auth/refresh ====================
	@Test
	@DisplayName("Refrescar token exitosamente")
	void testRefreshTokenSuccess() throws Exception {
		JwtResponse refreshedResponse = new JwtResponse();
		refreshedResponse.setToken("new_access_token_123");
		refreshedResponse.setType("Bearer");
		refreshedResponse.setEmail("juan@example.com");

		when(authService.refreshAccessToken("refresh_token_123")).thenReturn(refreshedResponse);

		mockMvc.perform(post("/api/auth/refresh")
				.with(csrf())
				.param("refreshToken", "refresh_token_123"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Token refrescado exitosamente"))
				.andExpect(jsonPath("$.data.token").value("new_access_token_123"));

		verify(authService, times(1)).refreshAccessToken("refresh_token_123");
	}

	@Test
	@DisplayName("Refrescar token falla por token inválido")
	void testRefreshTokenFailureInvalidToken() throws Exception {
		when(authService.refreshAccessToken(any()))
				.thenThrow(new IllegalArgumentException("Refresh token inválido o expirado"));

		mockMvc.perform(post("/api/auth/refresh")
				.with(csrf())
				.param("refreshToken", "invalid_token"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false));

		verify(authService, times(1)).refreshAccessToken("invalid_token");
	}

	// ==================== POST /api/auth/logout ====================
	@Test
	@DisplayName("Logout exitoso")
	void testLogoutSuccess() throws Exception {
		doNothing().when(authService).logout(any(), any());

		mockMvc.perform(post("/api/auth/logout")
				.with(csrf())
				.param("accessToken", "access_token_123")
				.param("refreshToken", "refresh_token_123"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Logout exitoso"));

		verify(authService, times(1)).logout("access_token_123", "refresh_token_123");
	}

	@Test
	@DisplayName("Logout sin parámetros")
	void testLogoutWithoutParams() throws Exception {
		doNothing().when(authService).logout(null, null);

		mockMvc.perform(post("/api/auth/logout")
				.with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));

		verify(authService, times(1)).logout(null, null);
	}

	@Test
	@DisplayName("Logout falla por error en servicio")
	void testLogoutFailure() throws Exception {
		doThrow(new RuntimeException("Error al hacer logout"))
				.when(authService).logout(any(), any());

		mockMvc.perform(post("/api/auth/logout")
				.with(csrf())
				.param("accessToken", "invalid_token"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));

		verify(authService, times(1)).logout("invalid_token", null);
	}

	// ==================== POST /api/auth/change-password ====================
	@Test
	@DisplayName("Cambiar contraseña exitosamente")
	@WithMockUser(username = "juan", roles = "USER")
	void testChangePasswordSuccess() throws Exception {
		ChangePasswordRequestDTO changePasswordRequest = new ChangePasswordRequestDTO();
		changePasswordRequest.setOldPassword("oldPassword123");
		changePasswordRequest.setNewPassword("newPassword123");
		changePasswordRequest.setConfirmPassword("newPassword123");

		doNothing().when(authService).changePassword(1L, changePasswordRequest);

		mockMvc.perform(post("/api/auth/change-password")
				.with(csrf())
				.param("userId", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(changePasswordRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Contraseña actualizada exitosamente"));

		verify(authService, times(1)).changePassword(1L, changePasswordRequest);
	}

	@Test
	@DisplayName("Cambiar contraseña falla por contraseña antigua incorrecta")
	@WithMockUser(username = "juan", roles = "USER")
	void testChangePasswordFailureWrongOldPassword() throws Exception {
		ChangePasswordRequestDTO changePasswordRequest = new ChangePasswordRequestDTO();
		changePasswordRequest.setOldPassword("wrongPassword");
		changePasswordRequest.setNewPassword("newPassword123");
		changePasswordRequest.setConfirmPassword("newPassword123");

		doThrow(new IllegalArgumentException("La contraseña antigua es incorrecta"))
				.when(authService).changePassword(any(), any());

		mockMvc.perform(post("/api/auth/change-password")
				.with(csrf())
				.param("userId", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(changePasswordRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	@DisplayName("Cambiar contraseña sin autenticación")
	void testChangePasswordUnauthorized() throws Exception {
		ChangePasswordRequestDTO changePasswordRequest = new ChangePasswordRequestDTO();
		changePasswordRequest.setOldPassword("oldPassword123");
		changePasswordRequest.setNewPassword("newPassword123");
		changePasswordRequest.setConfirmPassword("newPassword123");

		mockMvc.perform(post("/api/auth/change-password")
				.with(csrf())
				.param("userId", "1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(changePasswordRequest)))
				.andExpect(status().isUnauthorized());

		verify(authService, never()).changePassword(any(), any());
	}

	// ==================== GET /api/auth/password-status/{userId} ====================
	@Test
	@DisplayName("Obtener estado de contraseña")
	@WithMockUser(username = "juan", roles = "USER")
	void testGetPasswordStatusSuccess() throws Exception {
		when(authService.isPasswordChangeRequired(1L)).thenReturn(false);
		when(authService.getDaysUntilPasswordExpiration(1L)).thenReturn(45);

		mockMvc.perform(get("/api/auth/password-status/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.changeRequired").value(false))
				.andExpect(jsonPath("$.data.daysRemaining").value(45))
				.andExpect(jsonPath("$.data.expirationDays").value(90));

		verify(authService, times(1)).isPasswordChangeRequired(1L);
		verify(authService, times(1)).getDaysUntilPasswordExpiration(1L);
	}

	@Test
	@DisplayName("Obtener estado de contraseña - cambio requerido")
	@WithMockUser(username = "juan", roles = "USER")
	void testGetPasswordStatusChangeRequired() throws Exception {
		when(authService.isPasswordChangeRequired(1L)).thenReturn(true);
		when(authService.getDaysUntilPasswordExpiration(1L)).thenReturn(0);

		mockMvc.perform(get("/api/auth/password-status/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.changeRequired").value(true))
				.andExpect(jsonPath("$.data.daysRemaining").value(0));
	}

	@Test
	@DisplayName("Obtener estado de contraseña sin autenticación")
	void testGetPasswordStatusUnauthorized() throws Exception {
		mockMvc.perform(get("/api/auth/password-status/1"))
				.andExpect(status().isUnauthorized());

		verify(authService, never()).isPasswordChangeRequired(any());
	}

	@Test
	@DisplayName("Obtener estado de contraseña - usuario no encontrado")
	@WithMockUser(username = "juan", roles = "USER")
	void testGetPasswordStatusUserNotFound() throws Exception {
		when(authService.isPasswordChangeRequired(999L))
				.thenThrow(new IllegalArgumentException("Usuario no encontrado"));

		mockMvc.perform(get("/api/auth/password-status/999"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}

	// ==================== POST /api/auth/forgot-password ====================
	@Test
	@DisplayName("Solicitud de reset de contraseña exitosa")
	void testForgotPasswordSuccess() throws Exception {
		doNothing().when(passwordResetService).generateResetToken("juan@example.com");

		mockMvc.perform(post("/api/auth/forgot-password")
				.with(csrf())
				.param("email", "juan@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").containsString("Si el email existe"));

		verify(passwordResetService, times(1)).generateResetToken("juan@example.com");
	}

	@Test
	@DisplayName("Solicitud de reset de contraseña - email no encontrado (respuesta genérica)")
	void testForgotPasswordEmailNotFound() throws Exception {
		doThrow(new RuntimeException("Email no encontrado"))
				.when(passwordResetService).generateResetToken("noexiste@example.com");

		mockMvc.perform(post("/api/auth/forgot-password")
				.with(csrf())
				.param("email", "noexiste@example.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").containsString("Si el email existe"));
	}

	// ==================== POST /api/auth/reset-password ====================
	@Test
	@DisplayName("Reset de contraseña exitoso")
	void testResetPasswordSuccess() throws Exception {
		ResetPasswordRequestDTO resetRequest = new ResetPasswordRequestDTO();
		resetRequest.setToken("reset_token_123");
		resetRequest.setNewPassword("newPassword123");
		resetRequest.setConfirmPassword("newPassword123");

		doNothing().when(passwordResetService)
				.resetPassword("reset_token_123", "newPassword123", "newPassword123");

		mockMvc.perform(post("/api/auth/reset-password")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(resetRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Contraseña reseteada exitosamente"));

		verify(passwordResetService, times(1))
				.resetPassword("reset_token_123", "newPassword123", "newPassword123");
	}

	@Test
	@DisplayName("Reset de contraseña falla por token inválido")
	void testResetPasswordFailureInvalidToken() throws Exception {
		ResetPasswordRequestDTO resetRequest = new ResetPasswordRequestDTO();
		resetRequest.setToken("invalid_token");
		resetRequest.setNewPassword("newPassword123");
		resetRequest.setConfirmPassword("newPassword123");

		doThrow(new IllegalArgumentException("Token de reset inválido o expirado"))
				.when(passwordResetService).resetPassword(any(), any(), any());

		mockMvc.perform(post("/api/auth/reset-password")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(resetRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	@DisplayName("Reset de contraseña falla por contraseñas no coincidentes")
	void testResetPasswordFailureMismatchedPasswords() throws Exception {
		ResetPasswordRequestDTO resetRequest = new ResetPasswordRequestDTO();
		resetRequest.setToken("reset_token_123");
		resetRequest.setNewPassword("newPassword123");
		resetRequest.setConfirmPassword("differentPassword");

		doThrow(new IllegalArgumentException("Las contraseñas no coinciden"))
				.when(passwordResetService).resetPassword(any(), any(), any());

		mockMvc.perform(post("/api/auth/reset-password")
				.with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(resetRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}

	// ==================== GET /api/auth/validate-reset-token ====================
	@Test
	@DisplayName("Validar token de reset - token válido")
	void testValidateResetTokenSuccess() throws Exception {
		when(passwordResetService.validateResetToken("valid_token")).thenReturn(true);

		mockMvc.perform(get("/api/auth/validate-reset-token")
				.param("token", "valid_token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").value(true))
				.andExpect(jsonPath("$.message").value("Token válido"));

		verify(passwordResetService, times(1)).validateResetToken("valid_token");
	}

	@Test
	@DisplayName("Validar token de reset - token inválido")
	void testValidateResetTokenInvalid() throws Exception {
		when(passwordResetService.validateResetToken("invalid_token")).thenReturn(false);

		mockMvc.perform(get("/api/auth/validate-reset-token")
				.param("token", "invalid_token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data").value(false));
	}

	@Test
	@DisplayName("Validar token de reset - token expirado")
	void testValidateResetTokenExpired() throws Exception {
		when(passwordResetService.validateResetToken("expired_token"))
				.thenThrow(new IllegalArgumentException("Token expirado"));

		mockMvc.perform(get("/api/auth/validate-reset-token")
				.param("token", "expired_token"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}
}


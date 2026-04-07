package com.mx.mbrl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequestDTO {
	@NotBlank(message = "El token es requerido")
	private String token;

	@NotBlank(message = "La nueva contraseña es requerida")
	@Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
	private String newPassword;

	@NotBlank(message = "La confirmación de contraseña es requerida")
	private String confirmPassword;
}


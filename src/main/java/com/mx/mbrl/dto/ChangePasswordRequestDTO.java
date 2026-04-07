package com.mx.mbrl.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequestDTO {
	@NotBlank(message = "La contraseña actual es requerida")
	private String oldPassword;

	@NotBlank(message = "La nueva contraseña es requerida")
	@Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
	private String newPassword;

	@NotBlank(message = "La confirmación de contraseña es requerida")
	private String confirmPassword;
}


package com.mx.mbrl.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
	@NotBlank(message = "El email es requerido")
	@Email(message = "El email debe ser válido")
	private String email;

	@NotBlank(message = "La contraseña es requerida")
	@Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
	private String password;
}


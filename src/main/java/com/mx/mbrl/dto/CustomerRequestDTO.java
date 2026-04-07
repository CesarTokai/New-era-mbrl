package com.mx.mbrl.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestDTO {
	@NotBlank(message = "El nombre es requerido")
	@Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
	private String name;

	@Email(message = "El email debe ser válido")
	@Size(max = 255, message = "El email no puede exceder 255 caracteres")
	private String email;

	@Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
	private String phone;

	@Size(max = 1000, message = "La dirección no puede exceder 1000 caracteres")
	private String address;

	@Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
	private String city;

	@Size(max = 100, message = "El estado no puede exceder 100 caracteres")
	private String state;

	@Size(max = 20, message = "El código postal no puede exceder 20 caracteres")
	private String postalCode;
}


package com.mx.mbrl.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDTO {

	@NotBlank(message = "El nombre de la categoría es requerido")
	@Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
	private String name;

	private String description;

	private Long parentCategoryId;
}


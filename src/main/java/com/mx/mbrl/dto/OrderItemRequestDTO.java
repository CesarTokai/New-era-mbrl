package com.mx.mbrl.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequestDTO {
	@NotNull(message = "El ID del producto es requerido")
	private Long productId;

	@NotNull(message = "La cantidad es requerida")
	@Positive(message = "La cantidad debe ser mayor a 0")
	private Integer quantity;
}


package com.mx.mbrl.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequestDTO {
	@NotNull(message = "El ID del producto es requerido")
	private Long productId;

	@Positive(message = "La cantidad debe ser mayor a 0")
	private Integer quantity;

	@NotNull(message = "El precio unitario es requerido")
	@DecimalMin(value = "0.01", message = "El precio unitario debe ser mayor a 0")
	private BigDecimal unitPrice;
}


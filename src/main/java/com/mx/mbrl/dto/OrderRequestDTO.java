package com.mx.mbrl.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
	@NotNull(message = "El ID del cliente es requerido")
	private Long customerId;

	private LocalDateTime deliveryDate;

	@Size(max = 500, message = "La dirección de envío no puede exceder 500 caracteres")
	private String shippingAddress;

	@Size(max = 1000, message = "Las notas no pueden exceder 1000 caracteres")
	private String notes;

	@NotEmpty(message = "Debe incluir al menos un artículo en la orden")
	@Valid
	private List<OrderItemRequestDTO> items;
}


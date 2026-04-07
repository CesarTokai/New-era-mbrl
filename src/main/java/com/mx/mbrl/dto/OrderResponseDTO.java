package com.mx.mbrl.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
	private Long id;
	private String customerName;
	private LocalDateTime orderDate;
	private BigDecimal totalAmount;
	private String status;
	private List<OrderItemResponseDTO> items;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class OrderItemResponseDTO {
		private Long productId;
		private String productName;
		private Integer quantity;
		private BigDecimal unitPrice;
		private BigDecimal totalPrice;
	}
}


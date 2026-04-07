package com.mx.mbrl.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LowStockAlertDTO {
	private Long productId;
	private String productName;
	private Integer currentStock;
	private Integer minStock;
}


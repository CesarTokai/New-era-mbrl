package com.mx.mbrl.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSalesDTO {
	private Long productId;
	private String productName;
	private Integer totalQuantitySold;
	private BigDecimal totalSales;
	private BigDecimal totalProfit;
}


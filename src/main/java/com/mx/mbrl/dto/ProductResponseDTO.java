package com.mx.mbrl.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
	private Long id;
	private String name;
	private String description;
	private BigDecimal price;
	private BigDecimal costPrice;
	private Integer stock;
	private Integer minStock;
	private String imageUrl;
	private String brandName;
	private String categoryName;
	private Boolean isActive;
}


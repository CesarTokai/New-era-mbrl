package com.mx.mbrl.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
	private Long brandId;
	private String brandName;
	private Long categoryId;
	private String categoryName;
	private Boolean isActive;
	private String color;
	private String material;
	private String dimensions;
	private List<String> imageUrls;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}


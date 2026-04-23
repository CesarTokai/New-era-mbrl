package com.mx.mbrl.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {
	@NotBlank(message = "El nombre del producto es requerido")
	@Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
	private String name;

	@Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
	private String description;

	@NotNull(message = "El precio es requerido")
	@DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
	private BigDecimal price;

	@NotNull(message = "El costo es requerido")
	@DecimalMin(value = "0.01", message = "El costo debe ser mayor a 0")
	private BigDecimal costPrice;

	@PositiveOrZero(message = "El stock no puede ser negativo")
	private Integer stock = 0;

	@PositiveOrZero(message = "El stock mínimo no puede ser negativo")
	private Integer minStock = 5;

	@Size(max = 500, message = "La URL de imagen no puede exceder 500 caracteres")
	private String imageUrl;

	@Size(max = 100, message = "El color no puede exceder 100 caracteres")
	private String color;

	@Size(max = 100, message = "El material no puede exceder 100 caracteres")
	private String material;

	@Size(max = 255, message = "Las dimensiones no pueden exceder 255 caracteres")
	private String dimensions;

	@Size(max = 10, message = "No se pueden registrar más de 10 imágenes")
	private List<String> imageUrls;

	private Long brandId;

	private Long categoryId;

	private Boolean isActive;
}


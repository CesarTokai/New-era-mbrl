package com.mx.mbrl.controller;

import com.mx.mbrl.dto.ApiResponse;
import com.mx.mbrl.dto.ProductRequestDTO;
import com.mx.mbrl.dto.ProductResponseDTO;
import com.mx.mbrl.entity.Product;
import com.mx.mbrl.entity.ProductImage;
import com.mx.mbrl.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@GetMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getAllProducts() {
		log.info("Obteniendo todos los productos");

		try {
			List<Product> products = productService.findAll();
			List<ProductResponseDTO> dtos = products.stream()
					.map(this::mapToResponseDTO)
					.collect(Collectors.toList());

			return ResponseEntity.ok(ApiResponse.success(dtos, "Productos obtenidos exitosamente"));
		} catch (Exception e) {
			log.error("Error obteniendo productos: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo productos", 500));
		}
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(@PathVariable Long id) {
		log.info("Obteniendo producto ID: {}", id);

		try {
			Product product = productService.findById(id);
			ProductResponseDTO dto = mapToResponseDTO(product);

			return ResponseEntity.ok(ApiResponse.success(dto, "Producto obtenido exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Producto no encontrado: {}", id);
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error(e.getMessage(), 404));
		} catch (Exception e) {
			log.error("Error obteniendo producto: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo producto", 500));
		}
	}

	@GetMapping("/low-stock")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getLowStockProducts() {
		log.info("Obteniendo productos con stock bajo");

		try {
			List<Product> products = productService.findLowStock();
			List<ProductResponseDTO> dtos = products.stream()
					.map(this::mapToResponseDTO)
					.collect(Collectors.toList());

			return ResponseEntity.ok(ApiResponse.success(dtos, "Productos con stock bajo"));
		} catch (Exception e) {
			log.error("Error obteniendo productos con stock bajo: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo productos", 500));
		}
	}

	@GetMapping("/{id}/related")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getRelatedProducts(
			@PathVariable Long id,
			@RequestParam(defaultValue = "5") int limit) {
		log.info("Obteniendo productos relacionados para ID: {}, limit: {}", id, limit);

		try {
			List<Product> relatedProducts = productService.getRelatedProducts(id, limit);
			List<ProductResponseDTO> dtos = relatedProducts.stream()
					.map(this::mapToResponseDTO)
					.collect(Collectors.toList());

			return ResponseEntity.ok(ApiResponse.success(dtos, "Productos relacionados obtenidos exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Producto no encontrado: {}", id);
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error(e.getMessage(), 404));
		} catch (Exception e) {
			log.error("Error obteniendo productos relacionados: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo productos relacionados", 500));
		}
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(@Valid @RequestBody ProductRequestDTO productRequestDTO) {
		log.info("Creando nuevo producto: {}", productRequestDTO.getName());

		try {
			Product product = productService.create(productRequestDTO);
			ProductResponseDTO dto = mapToResponseDTO(product);

			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(dto, "Producto creado exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error creando producto: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error creando producto: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error creando producto", 500));
		}
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(@PathVariable Long id,
			@Valid @RequestBody ProductRequestDTO productRequestDTO) {
		log.info("Actualizando producto ID: {}", id);

		try {
			Product product = productService.update(id, productRequestDTO);
			ProductResponseDTO dto = mapToResponseDTO(product);

			return ResponseEntity.ok(ApiResponse.success(dto, "Producto actualizado exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error actualizando producto: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error actualizando producto: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error actualizando producto", 500));
		}
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
		log.info("Eliminando producto ID: {}", id);

		try {
			productService.delete(id);

			return ResponseEntity.ok(ApiResponse.success(null, "Producto eliminado exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error eliminando producto: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error eliminando producto: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error eliminando producto", 500));
		}
	}

	private ProductResponseDTO mapToResponseDTO(Product product) {
		List<String> imageUrls = product.getImages().stream()
				.map(ProductImage::getImageUrl)
				.collect(Collectors.toList());

		return new ProductResponseDTO(
				product.getId(),
				product.getName(),
				product.getDescription(),
				product.getPrice(),
				product.getCostPrice(),
				product.getStock(),
				product.getMinStock(),
				product.getImageUrl(),
				product.getBrand() != null ? product.getBrand().getId() : null,
				product.getBrand() != null ? product.getBrand().getName() : null,
				product.getCategory() != null ? product.getCategory().getId() : null,
				product.getCategory() != null ? product.getCategory().getName() : null,
				product.getIsActive(),
				product.getColor(),
				product.getMaterial(),
				product.getDimensions(),
				imageUrls,
				product.getCreatedAt(),
				product.getUpdatedAt()
		);
	}
}


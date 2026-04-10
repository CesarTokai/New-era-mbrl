package com.mx.mbrl.controller;

import com.mx.mbrl.dto.ApiResponse;
import com.mx.mbrl.dto.ProductRequestDTO;
import com.mx.mbrl.dto.ProductResponseDTO;
import com.mx.mbrl.entity.Product;
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

/**
 * Controlador que expone los productos bajo la ruta /furniture/
 * para compatibilidad con el frontend existente.
 */
@Slf4j
@RestController
@RequestMapping("/furniture")
@RequiredArgsConstructor
public class FurnitureController {

	private final ProductService productService;

	@GetMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getAllFurniture() {
		log.info("GET /furniture - Obteniendo todos los productos");
		try {
			List<ProductResponseDTO> dtos = productService.findAll()
					.stream().map(this::toDTO).collect(Collectors.toList());
			return ResponseEntity.ok(ApiResponse.success(dtos, "Productos obtenidos exitosamente"));
		} catch (Exception e) {
			log.error("Error obteniendo productos: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo productos", 500));
		}
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<ProductResponseDTO>> getFurnitureById(@PathVariable Long id) {
		log.info("GET /furniture/{} - Obteniendo producto", id);
		try {
			ProductResponseDTO dto = toDTO(productService.findById(id));
			return ResponseEntity.ok(ApiResponse.success(dto, "Producto obtenido exitosamente"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error(e.getMessage(), 404));
		} catch (Exception e) {
			log.error("Error obteniendo producto {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo producto", 500));
		}
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ProductResponseDTO>> createFurniture(
			@Valid @RequestBody ProductRequestDTO dto) {
		log.info("POST /furniture - Creando producto: {}", dto.getName());
		try {
			ProductResponseDTO result = toDTO(productService.create(dto));
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(result, "Producto creado exitosamente"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error creando producto: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error creando producto", 500));
		}
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ProductResponseDTO>> updateFurniture(
			@PathVariable Long id, @Valid @RequestBody ProductRequestDTO dto) {
		log.info("PUT /furniture/{} - Actualizando producto", id);
		try {
			ProductResponseDTO result = toDTO(productService.update(id, dto));
			return ResponseEntity.ok(ApiResponse.success(result, "Producto actualizado exitosamente"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error actualizando producto {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error actualizando producto", 500));
		}
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> deleteFurniture(@PathVariable Long id) {
		log.info("DELETE /furniture/{} - Eliminando producto", id);
		try {
			productService.delete(id);
			return ResponseEntity.ok(ApiResponse.success(null, "Producto eliminado exitosamente"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error eliminando producto {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error eliminando producto", 500));
		}
	}

	private ProductResponseDTO toDTO(Product product) {
		return new ProductResponseDTO(
				product.getId(),
				product.getName(),
				product.getDescription(),
				product.getPrice(),
				product.getCostPrice(),
				product.getStock(),
				product.getMinStock(),
				product.getImageUrl(),
				product.getBrand() != null ? product.getBrand().getName() : null,
				product.getCategory() != null ? product.getCategory().getName() : null,
				product.getIsActive()
		);
	}
}


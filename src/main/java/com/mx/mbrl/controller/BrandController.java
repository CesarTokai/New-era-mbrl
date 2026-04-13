package com.mx.mbrl.controller;

import com.mx.mbrl.dto.ApiResponse;
import com.mx.mbrl.dto.BrandRequestDTO;
import com.mx.mbrl.entity.Brand;
import com.mx.mbrl.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de marcas bajo /furniture/brands
 */
@Slf4j
@RestController
@RequestMapping("/furniture/brands")
@RequiredArgsConstructor
public class BrandController {

	private final BrandRepository brandRepository;

	@GetMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<List<Brand>>> getAllBrands() {
		log.info("GET /furniture/brands - Obteniendo todas las marcas");
		try {
			List<Brand> brands = brandRepository.findAll();
			return ResponseEntity.ok(ApiResponse.success(brands, "Marcas obtenidas exitosamente"));
		} catch (Exception e) {
			log.error("Error obteniendo marcas: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo marcas", 500));
		}
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Brand>> getBrandById(@PathVariable Long id) {
		try {
			Brand brand = brandRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException("Marca no encontrada con ID: " + id));
			return ResponseEntity.ok(ApiResponse.success(brand, "Marca obtenida exitosamente"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error(e.getMessage(), 404));
		}
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Brand>> createBrand(@RequestBody BrandRequestDTO dto) {
		log.info("POST /furniture/brands - Creando marca: {}", dto.getName());
		try {
			if (brandRepository.findByName(dto.getName()).isPresent()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(ApiResponse.error("Ya existe una marca con ese nombre", 400));
			}
			Brand brand = new Brand();
			brand.setName(dto.getName());
			Brand saved = brandRepository.save(brand);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(saved, "Marca creada exitosamente"));
		} catch (Exception e) {
			log.error("Error creando marca: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error creando marca", 500));
		}
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Brand>> updateBrand(
			@PathVariable Long id, @RequestBody BrandRequestDTO dto) {
		try {
			Brand brand = brandRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException("Marca no encontrada con ID: " + id));
			brand.setName(dto.getName());
			Brand updated = brandRepository.save(brand);
			return ResponseEntity.ok(ApiResponse.success(updated, "Marca actualizada exitosamente"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		}
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable Long id) {
		try {
			Brand brand = brandRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException("Marca no encontrada con ID: " + id));
			brandRepository.delete(brand);
			return ResponseEntity.ok(ApiResponse.success(null, "Marca eliminada exitosamente"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		}
	}
}

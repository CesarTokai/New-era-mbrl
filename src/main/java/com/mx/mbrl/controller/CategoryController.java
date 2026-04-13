package com.mx.mbrl.controller;

import com.mx.mbrl.dto.ApiResponse;
import com.mx.mbrl.dto.CategoryRequestDTO;
import com.mx.mbrl.entity.Category;
import com.mx.mbrl.repository.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de categorías bajo /furniture/categories
 * para compatibilidad con el frontend existente.
 */
@Slf4j
@RestController
@RequestMapping("/furniture/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryRepository categoryRepository;

	@GetMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
		log.info("GET /furniture/categories - Obteniendo todas las categorías");
		try {
			List<Category> categories = categoryRepository.findAll();
			return ResponseEntity.ok(ApiResponse.success(categories, "Categorías obtenidas exitosamente"));
		} catch (Exception e) {
			log.error("Error obteniendo categorías: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo categorías", 500));
		}
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable Long id) {
		log.info("GET /furniture/categories/{} - Obteniendo categoría", id);
		try {
			Category category = categoryRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));
			return ResponseEntity.ok(ApiResponse.success(category, "Categoría obtenida exitosamente"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error(e.getMessage(), 404));
		} catch (Exception e) {
			log.error("Error obteniendo categoría {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo categoría", 500));
		}
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Category>> createCategory(
			@Valid @RequestBody CategoryRequestDTO dto) {
		log.info("POST /furniture/categories - Creando categoría: {}", dto.getName());
		try {
			// Verificar que no exista una categoría con el mismo nombre
			if (categoryRepository.findByName(dto.getName()).isPresent()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(ApiResponse.error("Ya existe una categoría con ese nombre", 400));
			}

			Category category = new Category();
			category.setName(dto.getName());
			category.setDescription(dto.getDescription());

			// Asignar categoría padre si se proporcionó
			if (dto.getParentCategoryId() != null) {
				Category parent = categoryRepository.findById(dto.getParentCategoryId())
						.orElseThrow(() -> new IllegalArgumentException(
								"Categoría padre no encontrada con ID: " + dto.getParentCategoryId()));
				category.setParentCategory(parent);
			}

			Category saved = categoryRepository.save(category);
			log.info("Categoría creada con ID: {}", saved.getId());
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(saved, "Categoría creada exitosamente"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error creando categoría: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error creando categoría", 500));
		}
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Category>> updateCategory(
			@PathVariable Long id, @Valid @RequestBody CategoryRequestDTO dto) {
		log.info("PUT /furniture/categories/{} - Actualizando categoría", id);
		try {
			Category category = categoryRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));

			category.setName(dto.getName());
			category.setDescription(dto.getDescription());

			if (dto.getParentCategoryId() != null) {
				Category parent = categoryRepository.findById(dto.getParentCategoryId())
						.orElseThrow(() -> new IllegalArgumentException(
								"Categoría padre no encontrada con ID: " + dto.getParentCategoryId()));
				category.setParentCategory(parent);
			} else {
				category.setParentCategory(null);
			}

			Category updated = categoryRepository.save(category);
			return ResponseEntity.ok(ApiResponse.success(updated, "Categoría actualizada exitosamente"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error actualizando categoría {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error actualizando categoría", 500));
		}
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
		log.info("DELETE /furniture/categories/{} - Eliminando categoría", id);
		try {
			Category category = categoryRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));
			categoryRepository.delete(category);
			return ResponseEntity.ok(ApiResponse.success(null, "Categoría eliminada exitosamente"));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error eliminando categoría {}: {}", id, e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error eliminando categoría", 500));
		}
	}
}


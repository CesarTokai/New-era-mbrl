package com.mx.mbrl.service;

import com.mx.mbrl.dto.ProductRequestDTO;
import com.mx.mbrl.entity.Brand;
import com.mx.mbrl.entity.Category;
import com.mx.mbrl.entity.Product;
import com.mx.mbrl.repository.BrandRepository;
import com.mx.mbrl.repository.CategoryRepository;
import com.mx.mbrl.repository.OrderItemRepository;
import com.mx.mbrl.repository.ProductRepository;
import com.mx.mbrl.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final BrandRepository brandRepository;
	private final CategoryRepository categoryRepository;
	private final OrderItemRepository orderItemRepository;
	private final ReviewRepository reviewRepository;

	@Transactional
	public Product create(ProductRequestDTO productRequestDTO) {
		log.info("Creando nuevo producto: {}", productRequestDTO.getName());

		// Validar Brand si se proporciona
		Brand brand = null;
		if (productRequestDTO.getBrandId() != null) {
			brand = brandRepository.findById(productRequestDTO.getBrandId())
					.orElseThrow(() -> new IllegalArgumentException("Marca no encontrada con ID: " + productRequestDTO.getBrandId()));
		}

		// Validar Category si se proporciona
		Category category = null;
		if (productRequestDTO.getCategoryId() != null) {
			category = categoryRepository.findById(productRequestDTO.getCategoryId())
					.orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + productRequestDTO.getCategoryId()));
		}

		Product product = new Product();
		product.setName(productRequestDTO.getName());
		product.setDescription(productRequestDTO.getDescription());
		product.setPrice(productRequestDTO.getPrice());
		product.setCostPrice(productRequestDTO.getCostPrice());
		product.setStock(productRequestDTO.getStock() != null ? productRequestDTO.getStock() : 0);
		product.setMinStock(productRequestDTO.getMinStock() != null ? productRequestDTO.getMinStock() : 5);
		product.setImageUrl(productRequestDTO.getImageUrl());
		product.setBrand(brand);
		product.setCategory(category);
		product.setIsActive(true);
		product.setCreatedAt(LocalDateTime.now());
		product.setUpdatedAt(LocalDateTime.now());

		Product savedProduct = productRepository.save(product);
		log.info("✅ Producto creado con ID: {}", savedProduct.getId());
		return savedProduct;
	}

	@Transactional
	public Product update(Long id, ProductRequestDTO productRequestDTO) {
		log.info("Actualizando producto con ID: {}", id);

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

		// Validar Brand si se proporciona
		if (productRequestDTO.getBrandId() != null) {
			Brand brand = brandRepository.findById(productRequestDTO.getBrandId())
					.orElseThrow(() -> new IllegalArgumentException("Marca no encontrada con ID: " + productRequestDTO.getBrandId()));
			product.setBrand(brand);
		}

		// Validar Category si se proporciona
		if (productRequestDTO.getCategoryId() != null) {
			Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
					.orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + productRequestDTO.getCategoryId()));
			product.setCategory(category);
		}

		// Actualizar campos
		product.setName(productRequestDTO.getName());
		product.setDescription(productRequestDTO.getDescription());
		product.setPrice(productRequestDTO.getPrice());
		product.setCostPrice(productRequestDTO.getCostPrice());
		if (productRequestDTO.getStock() != null) {
			product.setStock(productRequestDTO.getStock());
		}
		if (productRequestDTO.getMinStock() != null) {
			product.setMinStock(productRequestDTO.getMinStock());
		}
		product.setImageUrl(productRequestDTO.getImageUrl());
		product.setUpdatedAt(LocalDateTime.now());

		Product updatedProduct = productRepository.save(product);
		log.info("Producto actualizado con ID: {}", updatedProduct.getId());

		return updatedProduct;
	}

	@Transactional
	public void delete(Long id) {
		log.info("Eliminando (soft delete) producto con ID: {}", id);

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

		// Soft delete
		product.setIsActive(false);
		product.setUpdatedAt(LocalDateTime.now());

		productRepository.save(product);
		log.info("Producto marcado como inactivo (soft delete): {}", id);
	}

	@Transactional(readOnly = true)
	public Product findById(Long id) {
		log.debug("Buscando producto con ID: {}", id);

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

		// Validar que no esté eliminado
		if (!product.getIsActive()) {
			throw new IllegalArgumentException("El producto ha sido eliminado");
		}

		return product;
	}

	@Transactional(readOnly = true)
	public List<Product> findAll() {
		log.debug("Obteniendo todos los productos activos");
		try {
			List<Product> products = productRepository.findByIsActiveTrue();
			log.info("✅ {} productos encontrados", products.size());
			return products;
		} catch (Exception e) {
			log.error("❌ ERROR EN findAll(): {}", e.getMessage(), e);
			throw e; // Re-lanzar para que se vea en los logs
		}
	}

	@Transactional(readOnly = true)
	public List<Product> findLowStock() {
		log.info("Obteniendo productos con stock bajo");
		return productRepository.findByStockLessThanEqualOrderByStockAsc(5);
	}

	@Transactional(readOnly = true)
	public List<Product> findByCategory(Long categoryId) {
		log.debug("Obteniendo productos de categoría: {}", categoryId);

		// Validar que la categoría existe
		categoryRepository.findById(categoryId)
				.orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + categoryId));

		return productRepository.findByCategoryId(categoryId);
	}

	@Transactional
	public Product incrementStock(Long productId, Integer quantity) {
		log.info("Incrementando stock del producto {} por: {} unidades", productId, quantity);

		Product product = findById(productId);
		product.setStock(product.getStock() + quantity);
		product.setUpdatedAt(LocalDateTime.now());

		return productRepository.save(product);
	}

	@Transactional
	public Product decrementStock(Long productId, Integer quantity) {
		log.info("Decrementando stock del producto {} por: {} unidades", productId, quantity);

		Product product = findById(productId);

		if (product.getStock() < quantity) {
			throw new IllegalArgumentException("Stock insuficiente para el producto ID: " + productId);
		}

		product.setStock(product.getStock() - quantity);
		product.setUpdatedAt(LocalDateTime.now());

		return productRepository.save(product);
	}

	@Transactional(readOnly = true)
	public List<Product> getRelatedProducts(Long productId, int limit) {
		log.info("Obteniendo productos relacionados para productId: {}, limit: {}", productId, limit);

		// Obtener el producto actual
		Product currentProduct = findById(productId);
		
		if (currentProduct == null) {
			log.warn("Producto no encontrado con ID: {}", productId);
			return List.of();
		}

		List<Product> relatedProducts = new java.util.ArrayList<>();

		// 1. Buscar productos de la misma categoría
		if (currentProduct.getCategory() != null) {
			List<Product> sameCategory = productRepository
					.findByCategoryIdExcludingProduct(currentProduct.getCategory().getId(), productId);
			relatedProducts.addAll(sameCategory);
		}

		// 2. Buscar productos con palabras clave similares en el nombre
		if (currentProduct.getName() != null && !currentProduct.getName().isEmpty()) {
			String[] keywords = currentProduct.getName().split("\\s+");
			for (String keyword : keywords) {
				if (keyword.length() > 2) { // Solo palabras de más de 2 caracteres
					List<Product> keywordMatches = productRepository.findByNameContainsIgnoreCase(keyword);
					relatedProducts.addAll(
							keywordMatches.stream()
									.filter(p -> !p.getId().equals(productId)) // Excluir el producto actual
									.filter(p -> !relatedProducts.contains(p)) // Evitar duplicados
									.collect(Collectors.toList())
					);
				}
			}
		}

		// 3. Eliminar duplicados
		Map<Long, Product> uniqueProducts = new HashMap<>();
		for (Product p : relatedProducts) {
			if (!uniqueProducts.containsKey(p.getId())) {
				uniqueProducts.put(p.getId(), p);
			}
		}

		// 4. Calcular puntuación de relevancia para ordenamiento
		Map<Product, Double> relevanceScores = new HashMap<>();
		for (Product product : uniqueProducts.values()) {
			double score = 0.0;

			// Puntuación por ventas (70% del peso)
			Long totalSales = orderItemRepository.countTotalSalesByProductId(product.getId());
			score += (totalSales != null ? totalSales : 0) * 0.7;

			// Puntuación por calificación promedio (30% del peso)
			Double avgRating = reviewRepository.getAverageRatingByProductId(product.getId());
			score += (avgRating != null ? avgRating : 0) * 0.3;

			relevanceScores.put(product, score);
		}

		// 5. Ordenar por puntuación de relevancia (descendente) y limitar
		List<Product> sortedProducts = uniqueProducts.values().stream()
				.sorted((p1, p2) -> Double.compare(
						relevanceScores.getOrDefault(p2, 0.0),
						relevanceScores.getOrDefault(p1, 0.0)
				))
				.limit(limit)
				.collect(Collectors.toList());

		log.info("Se encontraron {} productos relacionados para productId: {}", sortedProducts.size(), productId);
		return sortedProducts;
	}
}


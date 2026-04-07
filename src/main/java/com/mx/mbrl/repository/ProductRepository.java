package com.mx.mbrl.repository;

import com.mx.mbrl.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findByCategoryId(Long categoryId);
	List<Product> findByIsActiveTrue();
	List<Product> findByStockLessThanEqualOrderByStockAsc(Integer minStock);
	
	@Query("SELECT p FROM Product p WHERE p.isActive = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	List<Product> findByNameContainsIgnoreCase(@Param("keyword") String keyword);
	
	@Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true AND p.id != :productId")
	List<Product> findByCategoryIdExcludingProduct(@Param("categoryId") Long categoryId, @Param("productId") Long productId);
}


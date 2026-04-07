package com.mx.mbrl.repository;

import com.mx.mbrl.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
	List<Review> findByProductIdAndIsApprovedTrue(Long productId);
	
	@Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
	Long countApprovedReviewsByProductId(@Param("productId") Long productId);
	
	@Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
	Double getAverageRatingByProductId(@Param("productId") Long productId);
}


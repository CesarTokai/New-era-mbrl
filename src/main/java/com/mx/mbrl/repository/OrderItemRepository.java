package com.mx.mbrl.repository;

import com.mx.mbrl.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
	@Query("SELECT oi.product.id, SUM(oi.quantity) as totalSold FROM OrderItem oi WHERE oi.product.id IN :productIds GROUP BY oi.product.id")
	List<Object[]> countSalesByProductIds(@Param("productIds") List<Long> productIds);
	
	@Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
	Long countTotalSalesByProductId(@Param("productId") Long productId);
}


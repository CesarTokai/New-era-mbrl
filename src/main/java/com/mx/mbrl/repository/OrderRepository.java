package com.mx.mbrl.repository;

import com.mx.mbrl.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
	List<Order> findByCustomerId(Long customerId);
	List<Order> findByStatus(Order.Status status);
	List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}


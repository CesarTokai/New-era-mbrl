package com.mx.mbrl.repository;

import com.mx.mbrl.entity.Return;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReturnRepository extends JpaRepository<Return, Long> {
	List<Return> findByOrderId(Long orderId);
	List<Return> findByStatus(Return.Status status);
}


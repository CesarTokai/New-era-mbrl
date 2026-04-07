package com.mx.mbrl.repository;

import com.mx.mbrl.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
	List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(Long productId);
}


package com.mx.mbrl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {
	public enum MovementType { ENTRADA, SALIDA, AJUSTE, VENTA }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "product_id")
	private Product product;

	@Enumerated(EnumType.STRING)
	@Column(name = "movement_type", nullable = false, length = 10)
	private MovementType movementType;

	@Column(nullable = false)
	private Integer quantity;

	@Column(name = "reference_type", length = 50)
	private String referenceType;

	@Column(name = "reference_id")
	private Long referenceId;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@ManyToOne
	@JoinColumn(name = "created_by")
	private User createdBy;

	@CreationTimestamp
	@Column(name = "created_at", columnDefinition = "TIMESTAMP")
	private LocalDateTime createdAt;
}


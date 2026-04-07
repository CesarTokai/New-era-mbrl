package com.mx.mbrl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Return {
	public enum Status { SOLICITADA, APROBADA, RECHAZADA, COMPLETADA }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "order_id")
	private Order order;

	@ManyToOne(optional = false)
	@JoinColumn(name = "product_id")
	private Product product;

	@Column(nullable = false)
	private Integer quantity;

	@Column(length = 255)
	private String reason;

	@Column(name = "refund_amount", precision = 10, scale = 2)
	private BigDecimal refundAmount;

	@Enumerated(EnumType.STRING)
	@Column(length = 15)
	private Status status = Status.SOLICITADA;

	@CreationTimestamp
	@Column(name = "requested_at", columnDefinition = "TIMESTAMP")
	private LocalDateTime requestedAt;

	@Column(name = "resolved_at", columnDefinition = "TIMESTAMP")
	private LocalDateTime resolvedAt;
}


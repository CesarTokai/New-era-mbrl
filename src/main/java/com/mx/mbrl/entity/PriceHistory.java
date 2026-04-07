package com.mx.mbrl.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "product_id")
	private Product product;

	@Column(name = "old_price", precision = 10, scale = 2)
	private BigDecimal oldPrice;

	@Column(name = "new_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal newPrice;

	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;

	@Column(name = "changed_at", columnDefinition = "TIMESTAMP")
	private LocalDateTime changedAt;
}


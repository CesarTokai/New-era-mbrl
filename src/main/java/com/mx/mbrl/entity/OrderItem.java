package com.mx.mbrl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
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

	@Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal unitPrice;

	@Column(name = "cost_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal costPrice;

	@Formula("quantity * unit_price")
	@Column(name = "total_price", precision = 12, scale = 2)
	private BigDecimal totalPrice;
}


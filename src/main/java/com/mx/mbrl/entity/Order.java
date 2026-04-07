package com.mx.mbrl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
	public enum Status { PENDIENTE, CONFIRMADA, ENVIADA, ENTREGADA, CANCELADA }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "customer_id")
	private Customer customer;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@CreationTimestamp
	@Column(name = "order_date", columnDefinition = "TIMESTAMP")
	private LocalDateTime orderDate;

	@Column(name = "delivery_date", columnDefinition = "TIMESTAMP")
	private LocalDateTime deliveryDate;

	@Column(precision = 12, scale = 2)
	private BigDecimal subtotal = BigDecimal.ZERO;

	@Column(precision = 12, scale = 2)
	private BigDecimal tax = BigDecimal.ZERO;

	@Column(name = "total_amount", precision = 12, scale = 2)
	private BigDecimal totalAmount = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(length = 15)
	private Status status = Status.PENDIENTE;

	@Column(name = "shipping_address", columnDefinition = "TEXT")
	private String shippingAddress;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@CreationTimestamp
	@Column(name = "created_at", columnDefinition = "TIMESTAMP")
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", columnDefinition = "TIMESTAMP")
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	private List<OrderItem> items;
}


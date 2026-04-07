package com.mx.mbrl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false, length = 255)
	private String name;

	@Column(length = 255)
	private String email;

	@Column(length = 20)
	private String phone;

	@Column(columnDefinition = "TEXT")
	private String address;

	@Column(length = 100)
	private String city;

	@Column(length = 100)
	private String state;

	@Column(name = "postal_code", length = 20)
	private String postalCode;

	@Column(name = "total_orders")
	private Integer totalOrders = 0;

	@Column(name = "total_spent", precision = 12, scale = 2)
	private BigDecimal totalSpent = BigDecimal.ZERO;

	@Column(name = "last_order_date", columnDefinition = "TIMESTAMP")
	private LocalDateTime lastOrderDate;

	@CreationTimestamp
	@Column(name = "created_at", columnDefinition = "TIMESTAMP")
	private LocalDateTime createdAt;
}


package com.mx.mbrl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
	public enum PaymentMethod { EFECTIVO, TARJETA, TRANSFERENCIA, OTRO }
	public enum Status { PENDIENTE, COMPLETADO, FALLIDO }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "order_id", unique = true)
	private Order order;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", nullable = false, length = 20)
	private PaymentMethod paymentMethod;

	@Column(name = "transaction_id", length = 255)
	private String transactionId;

	@Enumerated(EnumType.STRING)
	@Column(length = 15)
	private Status status = Status.PENDIENTE;

	@Column(name = "payment_date", columnDefinition = "TIMESTAMP")
	private LocalDateTime paymentDate;

	@CreationTimestamp
	@Column(name = "created_at", columnDefinition = "TIMESTAMP")
	private LocalDateTime createdAt;
}


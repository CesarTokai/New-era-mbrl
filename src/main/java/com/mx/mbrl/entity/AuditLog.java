package com.mx.mbrl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
	public enum Operation { INSERT, UPDATE, DELETE }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "table_name", nullable = false, length = 100)
	private String tableName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Operation operation;

	@Column(name = "record_id")
	private Long recordId;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "old_values", columnDefinition = "JSON")
	private String oldValues;

	@Column(name = "new_values", columnDefinition = "JSON")
	private String newValues;

	@CreationTimestamp
	@Column(name = "operation_date", columnDefinition = "TIMESTAMP")
	private LocalDateTime operationDate;
}


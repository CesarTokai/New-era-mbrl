package com.mx.mbrl.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "old_password_hash", nullable = false, columnDefinition = "LONGTEXT")
	private String oldPasswordHash;

	@Column(name = "new_password_hash", nullable = false, columnDefinition = "LONGTEXT")
	private String newPasswordHash;

	@Column(name = "changed_at", nullable = false)
	private LocalDateTime changedAt;

	@Column(name = "reason")
	private String reason; // "USER_REQUEST", "FORCED_CHANGE", "SECURITY"

	@Column(name = "ip_address")
	private String ipAddress;
}


package com.mx.mbrl.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false, unique = true, columnDefinition = "LONGTEXT")
	private String token;

	@Column(name = "expiry_date", nullable = false)
	private LocalDateTime expiryDate;

	@Column(name = "used", nullable = false)
	private Boolean used = false;

	@Column(name = "used_at")
	private LocalDateTime usedAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	public boolean isValid() {
		return !used && LocalDateTime.now().isBefore(expiryDate);
	}

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiryDate);
	}
}


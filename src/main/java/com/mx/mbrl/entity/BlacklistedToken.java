package com.mx.mbrl.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blacklisted_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistedToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, columnDefinition = "LONGTEXT")
	private String token;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "invalidated_at", nullable = false)
	private LocalDateTime invalidatedAt;

	@Column(name = "reason")
	private String reason;

	@Column(name = "expires_at")
	private LocalDateTime expiresAt;
}


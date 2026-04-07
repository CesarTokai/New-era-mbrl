package com.mx.mbrl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
	public enum Role { USER, ADMIN }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String username;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(nullable = false, length = 255)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Role role = Role.USER;

	@CreationTimestamp
	@Column(name = "created_at", columnDefinition = "TIMESTAMP")
	private LocalDateTime createdAt;

	@Column(name = "last_password_change_date")
	private LocalDateTime lastPasswordChangeDate;

	public boolean isPasswordChangeRequired() {
		if (lastPasswordChangeDate == null) {
			return true; // Nunca ha cambiado, forzar cambio
		}
		LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
		return lastPasswordChangeDate.isBefore(ninetyDaysAgo);
	}
}


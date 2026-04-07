package com.mx.mbrl.repository;

import com.mx.mbrl.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.time.LocalDateTime;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
	Optional<PasswordResetToken> findByToken(String token);
	Optional<PasswordResetToken> findByUserIdAndUsedFalse(Long userId);
	void deleteByExpiryDateBefore(LocalDateTime expiryTime);
	void deleteByUserId(Long userId);
}


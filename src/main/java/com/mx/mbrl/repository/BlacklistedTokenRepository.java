package com.mx.mbrl.repository;

import com.mx.mbrl.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.time.LocalDateTime;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
	Optional<BlacklistedToken> findByToken(String token);
	boolean existsByToken(String token);
	void deleteByExpiresAtBefore(LocalDateTime expiryTime);
}


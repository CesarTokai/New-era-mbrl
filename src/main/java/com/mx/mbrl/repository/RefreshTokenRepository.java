package com.mx.mbrl.repository;

import com.mx.mbrl.entity.RefreshToken;
import com.mx.mbrl.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByToken(String token);
	List<RefreshToken> findByUser(User user);
	List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);
	void deleteByToken(String token);
	void deleteByUserId(Long userId);
}


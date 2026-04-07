package com.mx.mbrl.repository;

import com.mx.mbrl.entity.PasswordHistory;
import com.mx.mbrl.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
	List<PasswordHistory> findByUserOrderByChangedAtDesc(User user);
	List<PasswordHistory> findByUserIdOrderByChangedAtDesc(Long userId);
}


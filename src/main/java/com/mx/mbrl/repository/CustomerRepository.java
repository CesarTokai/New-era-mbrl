package com.mx.mbrl.repository;

import com.mx.mbrl.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	Optional<Customer> findByEmail(String email);
	Optional<Customer> findByPhone(String phone);
}


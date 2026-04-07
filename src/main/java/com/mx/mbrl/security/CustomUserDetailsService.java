package com.mx.mbrl.security;

import com.mx.mbrl.entity.User;
import com.mx.mbrl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		log.debug("Cargando usuario: {}", email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> {
					log.warn("Usuario no encontrado con email: {}", email);
					return new UsernameNotFoundException("Usuario no encontrado con email: " + email);
				});

		log.debug("Usuario encontrado: {} con rol: {}", email, user.getRole());

		// Crear autoridades basadas en el rol
		List<GrantedAuthority> authorities = Collections.singletonList(
				new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
		);

		return org.springframework.security.core.userdetails.User.builder()
				.username(user.getEmail())
				.password(user.getPassword())
				.authorities(authorities)
				.accountExpired(false)
				.accountLocked(false)
				.credentialsExpired(false)
				.disabled(false)
				.build();
	}
}


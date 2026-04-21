package com.mx.mbrl.service;

import com.mx.mbrl.dto.ChangePasswordRequestDTO;
import com.mx.mbrl.dto.CustomerRequestDTO;
import com.mx.mbrl.dto.JwtResponse;
import com.mx.mbrl.dto.LoginRequestDTO;
import com.mx.mbrl.entity.Customer;
import com.mx.mbrl.entity.User;
import com.mx.mbrl.repository.CustomerRepository;
import com.mx.mbrl.repository.UserRepository;
import com.mx.mbrl.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final CustomerRepository customerRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

	@Value("${jwt.expiration:86400000}")
	private long jwtExpirationMs;

	@Transactional
	public Customer register(CustomerRequestDTO customerRequestDTO) {
		log.info("Registrando nuevo cliente: {}", customerRequestDTO.getName());

		if (userRepository.findByEmail(customerRequestDTO.getEmail()).isPresent()) {
			throw new IllegalArgumentException("El email ya está registrado");
		}

		User user = new User();
		user.setUsername(customerRequestDTO.getEmail().split("@")[0]);
		user.setEmail(customerRequestDTO.getEmail());
		user.setPassword(passwordEncoder.encode(customerRequestDTO.getPassword()));
		user.setRole(User.Role.USER);

		User savedUser = userRepository.save(user);
		log.info("Usuario creado con ID: {}", savedUser.getId());

		Customer customer = new Customer();
		customer.setUser(savedUser);
		customer.setName(customerRequestDTO.getName());
		customer.setEmail(customerRequestDTO.getEmail());
		customer.setPhone(customerRequestDTO.getPhone());
		customer.setAddress(customerRequestDTO.getAddress());
		customer.setCity(customerRequestDTO.getCity());
		customer.setState(customerRequestDTO.getState());
		customer.setPostalCode(customerRequestDTO.getPostalCode());

		Customer savedCustomer = customerRepository.save(customer);
		log.info("Cliente creado con ID: {}", savedCustomer.getId());

		return savedCustomer;
	}

	@Transactional
	public JwtResponse login(LoginRequestDTO loginRequestDTO) {
		log.info("Login para usuario: {}", loginRequestDTO.getEmail());

		User user = userRepository.findByEmail(loginRequestDTO.getEmail())
				.orElseThrow(() -> new IllegalArgumentException("Email o contraseña incorrectos"));

		if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
			log.warn("Intento de login fallido para email: {}", loginRequestDTO.getEmail());
			throw new IllegalArgumentException("Email o contraseña incorrectos");
		}

		// Generar access token JWT — el cliente lo usa en el header Authorization: Bearer <token>
		String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

		log.info("Login exitoso para usuario: {}", user.getEmail());

		JwtResponse response = new JwtResponse();
		response.setAccessToken(accessToken);
		response.setRefreshToken(null);
		response.setType("Bearer");
		response.setId(user.getId());
		response.setUsername(user.getUsername());
		response.setEmail(user.getEmail());
		response.setRole(user.getRole().name());
		response.setExpiresIn(jwtExpirationMs);

		return response;
	}

	/**
	 * Logout: el token expira naturalmente según jwt.expiration.
	 * El cliente simplemente descarta el token localmente.
	 */
	@Transactional
	public void logout(String accessToken, String refreshToken) {
		log.info("Logout de usuario — token descartado del lado del cliente");
	}

	@Transactional
	public void changePassword(Long userId, ChangePasswordRequestDTO changePasswordRequest) {
		log.info("Cambiando contraseña para usuario ID: {}", userId);

		if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
			throw new IllegalArgumentException("Las contraseñas no coinciden");
		}

		if (changePasswordRequest.getCurrentPassword().equals(changePasswordRequest.getNewPassword())) {
			throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la actual");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

		if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
			log.warn("Intento fallido de cambio de contraseña para usuario ID: {}", userId);
			throw new IllegalArgumentException("La contraseña actual es incorrecta");
		}

		user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
		userRepository.save(user);

		log.info("Contraseña actualizada exitosamente para usuario ID: {}", userId);
	}
}

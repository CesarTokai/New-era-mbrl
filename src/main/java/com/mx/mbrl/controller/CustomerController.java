package com.mx.mbrl.controller;

import com.mx.mbrl.dto.ApiResponse;
import com.mx.mbrl.dto.CustomerRequestDTO;
import com.mx.mbrl.entity.Customer;
import com.mx.mbrl.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

	private final CustomerService customerService;

	@GetMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<List<Customer>>> getAllCustomers() {
		log.info("Obteniendo todos los clientes");

		try {
			List<Customer> customers = customerService.findAll();

			return ResponseEntity.ok(ApiResponse.success(customers, "Clientes obtenidos exitosamente"));
		} catch (Exception e) {
			log.error("Error obteniendo clientes: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo clientes", 500));
		}
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Customer>> getCustomerById(@PathVariable Long id) {
		log.info("Obteniendo cliente ID: {}", id);

		try {
			Customer customer = customerService.findById(id);

			return ResponseEntity.ok(ApiResponse.success(customer, "Cliente obtenido exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Cliente no encontrado: {}", id);
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error(e.getMessage(), 404));
		} catch (Exception e) {
			log.error("Error obteniendo cliente: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo cliente", 500));
		}
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Customer>> createCustomer(@Valid @RequestBody CustomerRequestDTO customerRequestDTO) {
		log.info("Creando nuevo cliente: {}", customerRequestDTO.getName());

		try {
			Customer customer = customerService.create(customerRequestDTO);

			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(customer, "Cliente creado exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error creando cliente: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error creando cliente: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error creando cliente", 500));
		}
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Customer>> updateCustomer(@PathVariable Long id,
			@Valid @RequestBody CustomerRequestDTO customerRequestDTO) {
		log.info("Actualizando cliente ID: {}", id);

		try {
			Customer customer = customerService.update(id, customerRequestDTO);

			return ResponseEntity.ok(ApiResponse.success(customer, "Cliente actualizado exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error actualizando cliente: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error actualizando cliente: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error actualizando cliente", 500));
		}
	}

	@GetMapping("/{id}/stats")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Customer>> getCustomerStats(@PathVariable Long id) {
		log.info("Obteniendo estadísticas del cliente ID: {}", id);

		try {
			Customer customer = customerService.getCustomerStats(id);

			return ResponseEntity.ok(ApiResponse.success(customer, "Estadísticas obtenidas exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Cliente no encontrado: {}", id);
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error(e.getMessage(), 404));
		} catch (Exception e) {
			log.error("Error obteniendo estadísticas: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo estadísticas", 500));
		}
	}
}


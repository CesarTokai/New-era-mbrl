package com.mx.mbrl.controller;

import com.mx.mbrl.dto.ApiResponse;
import com.mx.mbrl.dto.OrderRequestDTO;
import com.mx.mbrl.entity.Order;
import com.mx.mbrl.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Order>> createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO,
			Authentication authentication) {
		log.info("Creando orden para cliente ID: {}", orderRequestDTO.getCustomerId());

		try {
			Long userId = null; // En una implementación real, extraerías el userId del token
			Order order = orderService.createOrder(orderRequestDTO, userId);

			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(order, "Orden creada exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error creando orden: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error creando orden: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error creando orden", 500));
		}
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Order>> getOrder(@PathVariable Long id) {
		log.info("Obteniendo orden ID: {}", id);

		try {
			Order order = orderService.getOrder(id);

			return ResponseEntity.ok(ApiResponse.success(order, "Orden obtenida exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Orden no encontrada: {}", id);
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(ApiResponse.error(e.getMessage(), 404));
		} catch (Exception e) {
			log.error("Error obteniendo orden: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo orden", 500));
		}
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<List<Order>>> getOrders(@RequestParam(required = false) Long customerId,
			Authentication authentication) {
		log.info("Obteniendo órdenes");

		try {
			List<Order> orders;

			// Si es ADMIN, puede ver todas las órdenes o filtrar por cliente
			if (authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
				if (customerId != null) {
					orders = orderService.getOrdersByCustomer(customerId);
				} else {
					// Obtener todas las órdenes (implementar en OrderService si es necesario)
					orders = List.of(); // Placeholder
				}
			} else {
				// Si es USER, ver solo sus órdenes
				// En implementación real, obtendría el customerId del usuario logueado
				orders = customerId != null ? orderService.getOrdersByCustomer(customerId) : List.of();
			}

			return ResponseEntity.ok(ApiResponse.success(orders, "Órdenes obtenidas exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error obteniendo órdenes: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error obteniendo órdenes: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo órdenes", 500));
		}
	}

	@PutMapping("/{id}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> updateOrderStatus(@PathVariable Long id,
			@RequestParam Order.Status status) {
		log.info("Actualizando estado de orden ID: {} a {}", id, status);

		try {
			orderService.updateStatus(id, status);

			return ResponseEntity.ok(ApiResponse.success(null, "Estado de orden actualizado exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error actualizando estado: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error actualizando estado: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error actualizando estado", 500));
		}
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long id) {
		log.info("Cancelando orden ID: {}", id);

		try {
			orderService.cancelOrder(id);

			return ResponseEntity.ok(ApiResponse.success(null, "Orden cancelada exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error cancelando orden: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error cancelando orden: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error cancelando orden", 500));
		}
	}
}


package com.mx.mbrl.controller;

import com.mx.mbrl.dto.ApiResponse;
import com.mx.mbrl.entity.InventoryMovement;
import com.mx.mbrl.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

	private final InventoryService inventoryService;

	@GetMapping("/movements/{productId}")
	public ResponseEntity<ApiResponse<List<InventoryMovement>>> getInventoryMovements(@PathVariable Long productId) {
		log.info("Obteniendo movimientos de inventario para producto ID: {}", productId);

		try {
			List<InventoryMovement> movements = inventoryService.getMovements(productId);

			return ResponseEntity.ok(ApiResponse.success(movements, "Movimientos obtenidos exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error obteniendo movimientos: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error obteniendo movimientos: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo movimientos", 500));
		}
	}

	@PostMapping("/adjust")
	public ResponseEntity<ApiResponse<Void>> adjustStock(
			@RequestParam Long productId,
			@RequestParam Integer newQuantity,
			@RequestParam String reason) {
		log.info("Ajustando stock del producto ID: {} a: {}", productId, newQuantity);

		try {
			inventoryService.adjustStock(productId, newQuantity, reason, null);

			return ResponseEntity.ok(ApiResponse.success(null, "Stock ajustado exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error ajustando stock: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error ajustando stock: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error ajustando stock", 500));
		}
	}

	@PostMapping("/add-stock")
	public ResponseEntity<ApiResponse<Void>> addStock(
			@RequestParam Long productId,
			@RequestParam Integer quantity,
			@RequestParam(defaultValue = "MANUAL") String referenceType,
			@RequestParam(required = false) String notes) {
		log.info("Agregando stock al producto ID: {}, cantidad: {}", productId, quantity);

		try {
			inventoryService.addStock(productId, quantity, referenceType, null, notes);

			return ResponseEntity.ok(ApiResponse.success(null, "Stock agregado exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error agregando stock: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error agregando stock: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error agregando stock", 500));
		}
	}

	@PostMapping("/remove-stock")
	public ResponseEntity<ApiResponse<Void>> removeStock(
			@RequestParam Long productId,
			@RequestParam Integer quantity,
			@RequestParam String reason) {
		log.info("Removiendo stock del producto ID: {}, cantidad: {}", productId, quantity);

		try {
			inventoryService.removeStock(productId, quantity, reason, null);

			return ResponseEntity.ok(ApiResponse.success(null, "Stock removido exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error removiendo stock: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error removiendo stock: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error removiendo stock", 500));
		}
	}

	@GetMapping("/available-stock/{productId}")
	public ResponseEntity<ApiResponse<Integer>> getAvailableStock(@PathVariable Long productId) {
		log.info("Obteniendo stock disponible para producto ID: {}", productId);

		try {
			Integer stock = inventoryService.getAvailableStock(productId);

			return ResponseEntity.ok(ApiResponse.success(stock, "Stock obtenido exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error obteniendo stock: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error obteniendo stock: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error obteniendo stock", 500));
		}
	}
}


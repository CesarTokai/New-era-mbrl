package com.mx.mbrl.service;

import com.mx.mbrl.entity.InventoryMovement;
import com.mx.mbrl.entity.Product;
import com.mx.mbrl.entity.User;
import com.mx.mbrl.repository.InventoryMovementRepository;
import com.mx.mbrl.repository.ProductRepository;
import com.mx.mbrl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

	private final InventoryMovementRepository inventoryMovementRepository;
	private final ProductService productService;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;

	@Transactional
	public void addStock(Long productId, Integer quantity, String referenceType, Long referenceId, String notes) {
		log.info("Agregando stock: productId={}, quantity={}, referenceType={}, referenceId={}", 
				productId, quantity, referenceType, referenceId);

		if (quantity <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
		}

		// Incrementar stock del producto
		Product product = productService.incrementStock(productId, quantity);

		// Crear movimiento de inventario
		InventoryMovement movement = new InventoryMovement();
		movement.setProduct(product);
		movement.setMovementType(InventoryMovement.MovementType.ENTRADA);
		movement.setQuantity(quantity);
		movement.setReferenceType(referenceType);
		movement.setReferenceId(referenceId);
		movement.setNotes(notes);
		movement.setCreatedAt(LocalDateTime.now());

		inventoryMovementRepository.save(movement);
		log.info("Movimiento de entrada registrado para producto ID: {}", productId);
	}

	@Transactional
	public void removeStock(Long productId, Integer quantity, String reason, Long userId) {
		log.info("Removiendo stock: productId={}, quantity={}, reason={}, userId={}", 
				productId, quantity, reason, userId);

		if (quantity <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
		}

		// Validar usuario si se proporciona
		User user = null;
		if (userId != null) {
			user = userRepository.findById(userId)
					.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));
		}

		// Decrementar stock del producto
		Product product = productService.decrementStock(productId, quantity);

		// Crear movimiento de inventario
		InventoryMovement movement = new InventoryMovement();
		movement.setProduct(product);
		movement.setMovementType(InventoryMovement.MovementType.SALIDA);
		movement.setQuantity(quantity);
		movement.setReferenceType("MANUAL");
		movement.setNotes(reason);
		movement.setCreatedBy(user);
		movement.setCreatedAt(LocalDateTime.now());

		inventoryMovementRepository.save(movement);
		log.info("Movimiento de salida registrado para producto ID: {}", productId);
	}

	@Transactional
	public void adjustStock(Long productId, Integer newQuantity, String reason, Long userId) {
		log.info("Ajustando stock: productId={}, newQuantity={}, reason={}", productId, newQuantity, reason);

		if (newQuantity < 0) {
			throw new IllegalArgumentException("La cantidad no puede ser negativa");
		}

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));

		Integer currentStock = product.getStock();
		Integer difference = newQuantity - currentStock;

		// Validar usuario si se proporciona
		User user = null;
		if (userId != null) {
			user = userRepository.findById(userId)
					.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));
		}

		// Actualizar stock
		product.setStock(newQuantity);
		productRepository.save(product);

		// Crear movimiento de ajuste
		InventoryMovement movement = new InventoryMovement();
		movement.setProduct(product);
		movement.setMovementType(InventoryMovement.MovementType.AJUSTE);
		movement.setQuantity(Math.abs(difference));
		movement.setReferenceType("ADJUSTMENT");
		movement.setNotes(String.format("%s (Anterior: %d, Nuevo: %d)", reason, currentStock, newQuantity));
		movement.setCreatedBy(user);
		movement.setCreatedAt(LocalDateTime.now());

		inventoryMovementRepository.save(movement);
		log.info("Ajuste de stock registrado para producto ID: {} (diferencia: {})", productId, difference);
	}

	@Transactional
	public void recordSale(Long productId, Integer quantity, Long orderId, Long userId) {
		log.info("Registrando venta: productId={}, quantity={}, orderId={}", productId, quantity, orderId);

		if (quantity <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
		}

		// Validar usuario
		User user = null;
		if (userId != null) {
			user = userRepository.findById(userId)
					.orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));
		}

		// Decrementar stock del producto
		Product product = productService.decrementStock(productId, quantity);

		// Crear movimiento de venta
		InventoryMovement movement = new InventoryMovement();
		movement.setProduct(product);
		movement.setMovementType(InventoryMovement.MovementType.VENTA);
		movement.setQuantity(quantity);
		movement.setReferenceType("ORDER");
		movement.setReferenceId(orderId);
		movement.setCreatedBy(user);
		movement.setCreatedAt(LocalDateTime.now());

		inventoryMovementRepository.save(movement);
		log.info("Movimiento de venta registrado para producto ID: {}, orden ID: {}", productId, orderId);
	}

	@Transactional(readOnly = true)
	public List<InventoryMovement> getMovements(Long productId) {
		log.debug("Obteniendo movimientos de inventario para producto ID: {}", productId);

		// Validar que el producto existe
		productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));

		return inventoryMovementRepository.findByProductIdOrderByCreatedAtDesc(productId);
	}

	@Transactional(readOnly = true)
	public boolean checkStock(Long productId, Integer requestedQuantity) {
		log.debug("Verificando stock: productId={}, requestedQuantity={}", productId, requestedQuantity);

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));

		return product.getStock() >= requestedQuantity;
	}

	@Transactional(readOnly = true)
	public Integer getAvailableStock(Long productId) {
		log.debug("Obteniendo stock disponible para producto ID: {}", productId);

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));

		return product.getStock();
	}

	@Transactional(readOnly = true)
	public Integer getReservedStock(Long productId) {
		log.debug("Obteniendo stock reservado (pedidos pendientes) para producto ID: {}", productId);

		// Este método puede expandirse para contar órdenes pendientes
		// Por ahora retorna 0
		return 0;
	}
}


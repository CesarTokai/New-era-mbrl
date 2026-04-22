package com.mx.mbrl.service;

import com.mx.mbrl.dto.OrderItemRequestDTO;
import com.mx.mbrl.dto.OrderRequestDTO;
import com.mx.mbrl.entity.*;
import com.mx.mbrl.repository.CustomerRepository;
import com.mx.mbrl.repository.OrderItemRepository;
import com.mx.mbrl.repository.OrderRepository;
import com.mx.mbrl.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final CustomerRepository customerRepository;
	private final InventoryService inventoryService;
	private final ProductService productService;

	private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10%

	@Transactional
	public Order createOrder(OrderRequestDTO orderRequestDTO, Long userId) {
		log.info("Creando nueva orden para customer ID: {}", orderRequestDTO.getCustomerId());

		// Validar cliente
		Customer customer = customerRepository.findById(orderRequestDTO.getCustomerId())
				.orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + orderRequestDTO.getCustomerId()));

		// Validar que hay items
		if (orderRequestDTO.getItems() == null || orderRequestDTO.getItems().isEmpty()) {
			throw new IllegalArgumentException("La orden debe contener al menos un producto");
		}

		// Verificar stock de todos los productos antes de crear la orden
		for (OrderItemRequestDTO itemDTO : orderRequestDTO.getItems()) {
			if (!inventoryService.checkStock(itemDTO.getProductId(), itemDTO.getQuantity())) {
				throw new IllegalArgumentException("Stock insuficiente para el producto ID: " + itemDTO.getProductId());
			}
		}

		// Crear orden
		Order order = new Order();
		order.setCustomer(customer);
		order.setUser(null); // Se establecería si hay usuario logueado
		order.setOrderDate(LocalDateTime.now());
		order.setDeliveryDate(orderRequestDTO.getDeliveryDate());
		order.setShippingAddress(orderRequestDTO.getShippingAddress());
		order.setNotes(orderRequestDTO.getNotes());
		order.setStatus(Order.Status.PENDIENTE);
		order.setSubtotal(BigDecimal.ZERO);
		order.setTax(BigDecimal.ZERO);
		order.setTotalAmount(BigDecimal.ZERO);

		// Guardar orden primero
		Order savedOrder = orderRepository.save(order);
		log.info("Orden creada con ID: {}", savedOrder.getId());

		// Procesar items y calcular totales
		BigDecimal subtotal = BigDecimal.ZERO;
		List<OrderItem> items = new ArrayList<>();

		for (OrderItemRequestDTO itemDTO : orderRequestDTO.getItems()) {
			Product product = productService.findById(itemDTO.getProductId());

			// Crear item de orden
			OrderItem orderItem = new OrderItem();
			orderItem.setOrder(savedOrder);
			orderItem.setProduct(product);
			orderItem.setQuantity(itemDTO.getQuantity());
			orderItem.setUnitPrice(product.getPrice());
			orderItem.setCostPrice(product.getCostPrice());

			OrderItem savedItem = orderItemRepository.save(orderItem);
			items.add(savedItem);

			// Sumar al subtotal
			BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(itemDTO.getQuantity()));
			subtotal = subtotal.add(itemTotal);

			// Descontar stock y registrar movimiento
			inventoryService.recordSale(itemDTO.getProductId(), itemDTO.getQuantity(), savedOrder.getId(), userId);
			log.debug("Stock descontado para producto ID: {}, cantidad: {}", itemDTO.getProductId(), itemDTO.getQuantity());
		}

		// Calcular tax y total
		BigDecimal tax = subtotal.multiply(TAX_RATE);
		BigDecimal total = subtotal.add(tax);

		// Actualizar orden con totales
		savedOrder.setItems(items);
		savedOrder.setSubtotal(subtotal);
		savedOrder.setTax(tax);
		savedOrder.setTotalAmount(total);

		Order updatedOrder = orderRepository.save(savedOrder);
		log.info("Orden actualizada con totales - Subtotal: {}, Tax: {}, Total: {}", subtotal, tax, total);

		// Actualizar estadísticas del cliente
		customer.setTotalOrders(customer.getTotalOrders() + 1);
		customer.setTotalSpent(customer.getTotalSpent().add(total));
		customer.setLastOrderDate(LocalDateTime.now());
		customerRepository.save(customer);
		log.info("Estadísticas del cliente actualizadas - Total órdenes: {}, Total gastado: {}", 
				customer.getTotalOrders(), customer.getTotalSpent());

		return updatedOrder;
	}

	@Transactional(readOnly = true)
	public Order getOrder(Long orderId) {
		log.debug("Obteniendo orden con ID: {}", orderId);

		return orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + orderId));
	}

	@Transactional
	public void updateStatus(Long orderId, Order.Status newStatus) {
		log.info("Actualizando estado de orden ID: {} a {}", orderId, newStatus);

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + orderId));

		// Validar transiciones de estado permitidas
		Order.Status currentStatus = order.getStatus();
		if (!isValidStatusTransition(currentStatus, newStatus)) {
			throw new IllegalArgumentException(String.format("No se puede cambiar de %s a %s", currentStatus, newStatus));
		}

		order.setStatus(newStatus);
		order.setUpdatedAt(LocalDateTime.now());

		orderRepository.save(order);
		log.info("Estado de orden actualizado: {} -> {}", currentStatus, newStatus);
	}

	@Transactional
	public void cancelOrder(Long orderId) {
		log.info("Cancelando orden con ID: {}", orderId);

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + orderId));

		// Validar que no esté ya cancelada o entregada
		if (order.getStatus() == Order.Status.CANCELADA) {
			throw new IllegalArgumentException("La orden ya ha sido cancelada");
		}
		if (order.getStatus() == Order.Status.ENTREGADA) {
			throw new IllegalArgumentException("No se puede cancelar una orden entregada");
		}

		// Restaurar stock de cada item
		for (OrderItem item : order.getItems()) {
			inventoryService.addStock(
					item.getProduct().getId(),
					item.getQuantity(),
					"CANCELLATION",
					orderId,
					"Stock restaurado por cancelación de orden"
			);
			log.debug("Stock restaurado para producto ID: {}, cantidad: {}", item.getProduct().getId(), item.getQuantity());
		}

		// Actualizar estado
		order.setStatus(Order.Status.CANCELADA);
		order.setUpdatedAt(LocalDateTime.now());

		Order cancelledOrder = orderRepository.save(order);

		// Actualizar estadísticas del cliente
		Customer customer = cancelledOrder.getCustomer();
		customer.setTotalOrders(Math.max(0, customer.getTotalOrders() - 1));
		customer.setTotalSpent(customer.getTotalSpent().subtract(cancelledOrder.getTotalAmount()));
		customerRepository.save(customer);

		log.info("Orden cancelada. Stock restaurado y estadísticas del cliente actualizadas");
	}

	@Transactional(readOnly = true)
	public List<Order> getAllOrders() {
		log.debug("Obteniendo todas las órdenes");
		return orderRepository.findAll();
	}

	@Transactional(readOnly = true)
	public List<Order> getOrdersByCustomer(Long customerId) {
		log.debug("Obteniendo órdenes del cliente ID: {}", customerId);

		// Validar que el cliente existe
		customerRepository.findById(customerId)
				.orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + customerId));

		return orderRepository.findByCustomerId(customerId);
	}

	@Transactional(readOnly = true)
	public List<Order> getOrdersByDateRange(LocalDate startDate, LocalDate endDate) {
		log.debug("Obteniendo órdenes entre {} y {}", startDate, endDate);

		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("La fecha de inicio no puede ser mayor a la fecha de fin");
		}

		// Convertir LocalDate a LocalDateTime (inicio del día y fin del día)
		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		return orderRepository.findByOrderDateBetween(startDateTime, endDateTime);
	}

	@Transactional(readOnly = true)
	public List<Order> getOrdersByStatus(Order.Status status) {
		log.debug("Obteniendo órdenes con estado: {}", status);

		return orderRepository.findByStatus(status);
	}

	private boolean isValidStatusTransition(Order.Status from, Order.Status to) {
		// PENDIENTE -> CONFIRMADA, CANCELADA
		// CONFIRMADA -> ENVIADA, CANCELADA
		// ENVIADA -> ENTREGADA
		// ENTREGADA -> (no transiciones)
		// CANCELADA -> (no transiciones)

		switch (from) {
			case PENDIENTE:
				return to == Order.Status.CONFIRMADA || to == Order.Status.CANCELADA;
			case CONFIRMADA:
				return to == Order.Status.ENVIADA || to == Order.Status.CANCELADA;
			case ENVIADA:
				return to == Order.Status.ENTREGADA;
			case ENTREGADA:
			case CANCELADA:
				return false; // Estados finales
			default:
				return false;
		}
	}
}



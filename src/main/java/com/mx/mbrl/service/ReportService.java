package com.mx.mbrl.service;

import com.mx.mbrl.dto.LowStockAlertDTO;
import com.mx.mbrl.dto.ProductSalesDTO;
import com.mx.mbrl.dto.SalesReportDTO;
import com.mx.mbrl.entity.Order;
import com.mx.mbrl.entity.OrderItem;
import com.mx.mbrl.entity.Product;
import com.mx.mbrl.repository.OrderRepository;
import com.mx.mbrl.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;

	@Transactional(readOnly = true)
	public List<SalesReportDTO> getSalesReport(LocalDate startDate, LocalDate endDate) {
		log.info("Generando reporte de ventas entre {} y {}", startDate, endDate);

		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("La fecha de inicio no puede ser mayor a la fecha de fin");
		}

		// Convertir LocalDate a LocalDateTime
		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		// Obtener órdenes en el rango de fechas
		List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);

		// Agrupar por fecha y calcular totales
		Map<LocalDate, SalesReportDTO> reportMap = new TreeMap<>();

		for (Order order : orders) {
			LocalDate orderDate = order.getOrderDate().toLocalDate();

			reportMap.computeIfAbsent(orderDate, date -> {
				SalesReportDTO report = new SalesReportDTO();
				report.setDate(date);
				report.setTotalOrders(0);
				report.setTotalSales(BigDecimal.ZERO);
				report.setProfit(BigDecimal.ZERO);
				return report;
			});

			SalesReportDTO report = reportMap.get(orderDate);
			report.setTotalOrders(report.getTotalOrders() + 1);
			report.setTotalSales(report.getTotalSales().add(order.getTotalAmount()));

			// Calcular ganancia (total - costo)
			BigDecimal profit = calculateOrderProfit(order);
			report.setProfit(report.getProfit().add(profit));
		}

		List<SalesReportDTO> result = new ArrayList<>(reportMap.values());
		log.info("Reporte de ventas generado: {} días con datos", result.size());

		return result;
	}

	@Transactional(readOnly = true)
	public List<ProductSalesDTO> getTopProducts(Integer limit, LocalDate startDate, LocalDate endDate) {
		log.info("Generando reporte de productos top {} entre {} y {}", limit, startDate, endDate);

		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("La fecha de inicio no puede ser mayor a la fecha de fin");
		}

		if (limit == null || limit <= 0) {
			limit = 10; // Default a top 10
		}

		// Convertir LocalDate a LocalDateTime
		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		// Obtener órdenes en el rango de fechas
		List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);

		// Agrupar items por producto
		Map<Long, ProductSalesDTO> productMap = new HashMap<>();

		for (Order order : orders) {
			for (OrderItem item : order.getItems()) {
				Long productId = item.getProduct().getId();

				productMap.computeIfAbsent(productId, id -> {
					ProductSalesDTO dto = new ProductSalesDTO();
					dto.setProductId(productId);
					dto.setProductName(item.getProduct().getName());
					dto.setTotalQuantitySold(0);
					dto.setTotalSales(BigDecimal.ZERO);
					dto.setTotalProfit(BigDecimal.ZERO);
					return dto;
				});

				ProductSalesDTO productSales = productMap.get(productId);
				productSales.setTotalQuantitySold(productSales.getTotalQuantitySold() + item.getQuantity());

				BigDecimal itemTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
				productSales.setTotalSales(productSales.getTotalSales().add(itemTotal));

				BigDecimal itemProfit = (item.getUnitPrice().subtract(item.getCostPrice()))
						.multiply(new BigDecimal(item.getQuantity()));
				productSales.setTotalProfit(productSales.getTotalProfit().add(itemProfit));
			}
		}

		// Ordenar por total de ventas descendente y limitar
		List<ProductSalesDTO> result = productMap.values().stream()
				.sorted((a, b) -> b.getTotalSales().compareTo(a.getTotalSales()))
				.limit(limit)
				.collect(Collectors.toList());

		log.info("Reporte de productos top generado: {} productos", result.size());

		return result;
	}

	@Transactional(readOnly = true)
	public List<LowStockAlertDTO> getLowStockReport() {
		log.info("Generando reporte de productos con stock bajo");

		// Obtener productos con stock bajo
		List<Product> lowStockProducts = productRepository.findByStockLessThanEqualOrderByStockAsc(5);

		// Convertir a DTOs
		List<LowStockAlertDTO> alerts = lowStockProducts.stream()
				.map(product -> new LowStockAlertDTO(
						product.getId(),
						product.getName(),
						product.getStock(),
						product.getMinStock()
				))
				.collect(Collectors.toList());

		log.info("Reporte de stock bajo generado: {} productos en alerta", alerts.size());

		return alerts;
	}

	@Transactional(readOnly = true)
	public BigDecimal getProfitByPeriod(LocalDate startDate, LocalDate endDate) {
		log.info("Calculando ganancia total entre {} y {}", startDate, endDate);

		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("La fecha de inicio no puede ser mayor a la fecha de fin");
		}

		// Convertir LocalDate a LocalDateTime
		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		// Obtener órdenes en el rango de fechas
		List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);

		// Calcular ganancia total
		BigDecimal totalProfit = BigDecimal.ZERO;

		for (Order order : orders) {
			BigDecimal profit = calculateOrderProfit(order);
			totalProfit = totalProfit.add(profit);
		}

		log.info("Ganancia total del período: {}", totalProfit);

		return totalProfit;
	}

	@Transactional(readOnly = true)
	public BigDecimal getRevenueByPeriod(LocalDate startDate, LocalDate endDate) {
		log.info("Calculando ingresos totales entre {} y {}", startDate, endDate);

		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("La fecha de inicio no puede ser mayor a la fecha de fin");
		}

		// Convertir LocalDate a LocalDateTime
		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		// Obtener órdenes en el rango de fechas
		List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);

		// Sumar totales de todas las órdenes
		BigDecimal totalRevenue = orders.stream()
				.map(Order::getTotalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		log.info("Ingresos totales del período: {}", totalRevenue);

		return totalRevenue;
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getDashboardMetrics(LocalDate startDate, LocalDate endDate) {
		log.info("Generando métricas de dashboard entre {} y {}", startDate, endDate);

		if (startDate.isAfter(endDate)) {
			throw new IllegalArgumentException("La fecha de inicio no puede ser mayor a la fecha de fin");
		}

		Map<String, Object> metrics = new LinkedHashMap<>();

		// Conversiones LocalDate a LocalDateTime
		LocalDateTime startDateTime = startDate.atStartOfDay();
		LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

		// Órdenes en el período
		List<Order> orders = orderRepository.findByOrderDateBetween(startDateTime, endDateTime);

		// Calcular métrica
		BigDecimal revenue = orders.stream()
				.map(Order::getTotalAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal profit = orders.stream()
				.map(this::calculateOrderProfit)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		Long totalOrders = (long) orders.size();
		BigDecimal avgOrderValue = totalOrders > 0 ? revenue.divide(new BigDecimal(totalOrders), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO;

		metrics.put("totalOrders", totalOrders);
		metrics.put("totalRevenue", revenue);
		metrics.put("totalProfit", profit);
		metrics.put("averageOrderValue", avgOrderValue);
		metrics.put("marginPercentage", totalOrders > 0 ? profit.divide(revenue, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100)) : BigDecimal.ZERO);

		log.info("Dashboard metrics calculadas: {} órdenes, ${} ingresos, ${} ganancia", totalOrders, revenue, profit);

		return metrics;
	}

	private BigDecimal calculateOrderProfit(Order order) {
		BigDecimal profit = BigDecimal.ZERO;

		for (OrderItem item : order.getItems()) {
			// Ganancia por item = (unitPrice - costPrice) * quantity
			BigDecimal itemProfit = item.getUnitPrice()
					.subtract(item.getCostPrice())
					.multiply(new BigDecimal(item.getQuantity()));
			profit = profit.add(itemProfit);
		}

		return profit;
	}
}


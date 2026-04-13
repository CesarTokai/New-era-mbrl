package com.mx.mbrl.controller;

import com.mx.mbrl.dto.ApiResponse;
import com.mx.mbrl.dto.LowStockAlertDTO;
import com.mx.mbrl.dto.ProductSalesDTO;
import com.mx.mbrl.dto.SalesReportDTO;
import com.mx.mbrl.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class ReportController {

	private final ReportService reportService;

	@GetMapping("/sales")
	public ResponseEntity<ApiResponse<List<SalesReportDTO>>> getSalesReport(
			@RequestParam LocalDate startDate,
			@RequestParam LocalDate endDate) {
		log.info("Generando reporte de ventas entre {} y {}", startDate, endDate);

		try {
			List<SalesReportDTO> report = reportService.getSalesReport(startDate, endDate);

			return ResponseEntity.ok(ApiResponse.success(report, "Reporte de ventas generado exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error generando reporte: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error generando reporte: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error generando reporte", 500));
		}
	}

	@GetMapping("/top-products")
	public ResponseEntity<ApiResponse<List<ProductSalesDTO>>> getTopProducts(
			@RequestParam(defaultValue = "10") Integer limit,
			@RequestParam LocalDate startDate,
			@RequestParam LocalDate endDate) {
		log.info("Generando reporte de productos top {} entre {} y {}", limit, startDate, endDate);

		try {
			List<ProductSalesDTO> report = reportService.getTopProducts(limit, startDate, endDate);

			return ResponseEntity.ok(ApiResponse.success(report, "Reporte de productos top generado exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error generando reporte: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error generando reporte: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error generando reporte", 500));
		}
	}

	@GetMapping("/low-stock")
	public ResponseEntity<ApiResponse<List<LowStockAlertDTO>>> getLowStockReport() {
		log.info("Generando reporte de stock bajo");

		try {
			List<LowStockAlertDTO> report = reportService.getLowStockReport();

			return ResponseEntity.ok(ApiResponse.success(report, "Reporte de stock bajo generado exitosamente"));
		} catch (Exception e) {
			log.error("Error generando reporte: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error generando reporte", 500));
		}
	}

	@GetMapping("/profit")
	public ResponseEntity<ApiResponse<BigDecimal>> getProfitByPeriod(
			@RequestParam LocalDate startDate,
			@RequestParam LocalDate endDate) {
		log.info("Calculando ganancia entre {} y {}", startDate, endDate);

		try {
			BigDecimal profit = reportService.getProfitByPeriod(startDate, endDate);

			return ResponseEntity.ok(ApiResponse.success(profit, "Ganancia calculada exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error calculando ganancia: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error calculando ganancia: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error calculando ganancia", 500));
		}
	}

	@GetMapping("/revenue")
	public ResponseEntity<ApiResponse<BigDecimal>> getRevenueByPeriod(
			@RequestParam LocalDate startDate,
			@RequestParam LocalDate endDate) {
		log.info("Calculando ingresos entre {} y {}", startDate, endDate);

		try {
			BigDecimal revenue = reportService.getRevenueByPeriod(startDate, endDate);

			return ResponseEntity.ok(ApiResponse.success(revenue, "Ingresos calculados exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error calculando ingresos: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error calculando ingresos: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error calculando ingresos", 500));
		}
	}

	@GetMapping("/dashboard")
	public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardMetrics(
			@RequestParam LocalDate startDate,
			@RequestParam LocalDate endDate) {
		log.info("Generando métricas de dashboard entre {} y {}", startDate, endDate);

		try {
			Map<String, Object> metrics = reportService.getDashboardMetrics(startDate, endDate);

			return ResponseEntity.ok(ApiResponse.success(metrics, "Métricas de dashboard generadas exitosamente"));
		} catch (IllegalArgumentException e) {
			log.error("Error generando métricas: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(ApiResponse.error(e.getMessage(), 400));
		} catch (Exception e) {
			log.error("Error generando métricas: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error generando métricas", 500));
		}
	}
}


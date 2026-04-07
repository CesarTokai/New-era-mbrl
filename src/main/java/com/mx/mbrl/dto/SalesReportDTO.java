package com.mx.mbrl.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportDTO {
	private LocalDate date;
	private Integer totalOrders;
	private BigDecimal totalSales;
	private BigDecimal profit;
}


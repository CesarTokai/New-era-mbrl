package com.mx.mbrl.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordStatusDTO {
	private boolean changeRequired;
	private int daysRemaining;
	private int expirationDays = 90;
}


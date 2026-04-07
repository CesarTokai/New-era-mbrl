package com.mx.mbrl.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitStatusDTO {
	private long tokensRemaining;
	private long retryAfterSeconds;
	private String message;
}


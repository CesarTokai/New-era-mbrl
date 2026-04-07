package com.mx.mbrl.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
	private String accessToken;
	private String refreshToken;
	private String type = "Bearer";
	private Long id;
	private String username;
	private String email;
	private String role;
	private Long expiresIn = 86400000L; // 24 horas en ms
}


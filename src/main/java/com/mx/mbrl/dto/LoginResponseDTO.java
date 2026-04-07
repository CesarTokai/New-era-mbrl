package com.mx.mbrl.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
	private String token;
	private String type = "Bearer";
	private Long id;
	private String username;
	private String email;
	private String role;
}


package com.mx.mbrl.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
	private boolean success;
	private String message;
	private T data;
	private LocalDateTime timestamp = LocalDateTime.now();
	private Integer code;

	public static <T> ApiResponse<T> success(T data, String message) {
		return ApiResponse.<T>builder()
				.success(true)
				.message(message)
				.data(data)
				.code(200)
				.build();
	}

	public static <T> ApiResponse<T> success(T data) {
		return success(data, "Operación exitosa");
	}

	public static <T> ApiResponse<T> error(String message, Integer code) {
		return ApiResponse.<T>builder()
				.success(false)
				.message(message)
				.code(code)
				.build();
	}
}


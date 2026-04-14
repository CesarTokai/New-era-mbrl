package com.mx.mbrl.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.mbrl.dto.ApiResponse;
import com.mx.mbrl.service.RateLimitingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {

	private final RateLimitingService rateLimitingService;
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// Rate limiting está deshabilitado para desarrollo local
		// En producción, descomentar la lógica de rate limiting
		filterChain.doFilter(request, response);
	}
}


package com.mx.mbrl.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.mbrl.dto.ApiResponse;
import com.mx.mbrl.service.RateLimitingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {

	private final RateLimitingService rateLimitingService;
	private final ObjectMapper objectMapper;

	private static final String LOGIN_ENDPOINT = "/api/auth/login";
	private static final String FORGOT_PASSWORD_ENDPOINT = "/api/auth/forgot-password";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String requestUri = request.getRequestURI();
		String method = request.getMethod();

		try {
			// Rate limiting deshabilitado para desarrollo local (solo localhost)
			// En producción, descomentar el código de rate limiting
			
			/*
			// Limitar rate limit para login y forgot-password (sin autenticación)
			if ((requestUri.contains(LOGIN_ENDPOINT) || requestUri.contains(FORGOT_PASSWORD_ENDPOINT)) 
					&& "POST".equals(method)) {
				String clientIp = getClientIp(request);

				if (!rateLimitingService.allowLogin(clientIp)) {
					log.warn("Rate limit de login excedido para IP: {}", clientIp);
					sendRateLimitExceededResponse(response, "Demasiados intentos de login. Intenta más tarde.", 429);
					return;
				}
			}

			// Limitar rate limit general para usuarios autenticados
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.isAuthenticated() && 
					!authentication.getPrincipal().equals("anonymousUser")) {
				String userId = authentication.getName();

				if (!rateLimitingService.allowRequest(userId)) {
					log.warn("Rate limit general excedido para usuario: {}", userId);
					sendRateLimitExceededResponse(response, "Has excedido el límite de requests. Intenta más tarde.", 429);
					return;
				}
			}
			*/

		} catch (Exception e) {
			log.error("Error en RateLimiterFilter: {}", e.getMessage());
			// No fallar si hay error - continuar con el request
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * Envía respuesta de rate limit excedido
	 */
	private void sendRateLimitExceededResponse(HttpServletResponse response, String message, int status)
			throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		ApiResponse<Void> apiResponse = ApiResponse.error(message, status);
		String jsonResponse = objectMapper.writeValueAsString(apiResponse);

		response.getWriter().write(jsonResponse);
		response.getWriter().flush();
	}

	/**
	 * Obtiene la IP del cliente, considerando proxies
	 */
	private String getClientIp(HttpServletRequest request) {
		String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};

		for (String header : headers) {
			String ip = request.getHeader(header);
			if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
				// Si hay múltiples IPs, usar la primera
				return ip.split(",")[0].trim();
			}
		}

		return request.getRemoteAddr();
	}
}


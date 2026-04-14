package com.mx.mbrl.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.mbrl.security.JwtAuthFilter;
import com.mx.mbrl.security.JwtUtil;
import com.mx.mbrl.security.RateLimiterFilter;
import com.mx.mbrl.service.RateLimitingService;
import com.mx.mbrl.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private TokenBlacklistService tokenBlacklistService;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private RateLimitingService rateLimitingService;

	@Autowired
	private ObjectMapper objectMapper;

	@Bean
	public JwtAuthFilter jwtAuthFilter() {
		return new JwtAuthFilter(jwtUtil, userDetailsService, tokenBlacklistService);
	}

	@Bean
	public RateLimiterFilter rateLimiterFilter() {
		return new RateLimiterFilter(rateLimitingService, objectMapper);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		log.info("Configurando BCryptPasswordEncoder");
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, 
			JwtAuthFilter jwtAuthFilter,
			RateLimiterFilter rateLimiterFilter) throws Exception {
		log.info("Configurando cadena de filtros de seguridad");

		http
				// Deshabilitar CSRF
				.csrf(csrf -> csrf.disable())

				// Configurar CORS
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))

				// Configurar sesiones stateless (JWT)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// Respuestas JSON para errores de autenticación/autorización
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((request, response, authException) -> {
							response.setContentType("application/json;charset=UTF-8");
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.getWriter().write("{\"success\":false,\"message\":\"Token inválido o expirado. Inicia sesión nuevamente.\",\"status\":401}");
						})
						.accessDeniedHandler((request, response, accessDeniedException) -> {
							response.setContentType("application/json;charset=UTF-8");
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
							response.getWriter().write("{\"success\":false,\"message\":\"No tienes permisos para realizar esta acción.\",\"status\":403}");
						})
				)

		// Configurar autorización de endpoints
		.authorizeHttpRequests(authz -> authz
				// ── Endpoints públicos (sin JWT) ──────────────────────────────────
				.requestMatchers(HttpMethod.POST,  "/api/auth/register").permitAll()
				.requestMatchers(HttpMethod.POST,  "/api/auth/login").permitAll()
				.requestMatchers(HttpMethod.POST,  "/api/auth/forgot-password").permitAll()
				.requestMatchers(HttpMethod.GET,   "/api/auth/validate-reset-token").permitAll()
				.requestMatchers(HttpMethod.POST,  "/api/auth/reset-password").permitAll()
				// Preflight CORS
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				// Imágenes públicas (lectura)
				.requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
				// Swagger / OpenAPI
				.requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
				.requestMatchers("/actuator/health").permitAll()

				// ── Endpoints protegidos (requieren JWT válido) ────────────────────
				// /furniture/** y /api/** — cualquier usuario autenticado (USER o ADMIN)
				.requestMatchers("/furniture/**").authenticated()
				.anyRequest().authenticated()
		)

			// RateLimiterFilter corre antes del JwtAuthFilter, y ambos antes de UsernamePasswordAuthenticationFilter
			.addFilterBefore(rateLimiterFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}


	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		log.info("Configurando CORS");

		CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.setAllowedOrigins(Arrays.asList(
			"http://localhost:3000",
			"http://localhost:4200",
			"http://localhost:5173",
			"http://localhost:5174",
			"http://127.0.0.1:3000",
			"http://127.0.0.1:4200",
			"http://127.0.0.1:5173",
			"http://127.0.0.1:5174"
		));
		corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		corsConfig.setAllowedHeaders(Arrays.asList("*"));
		corsConfig.setAllowCredentials(true);
		corsConfig.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfig);

		return source;
	}
}


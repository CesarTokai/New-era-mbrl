package com.mx.mbrl.config;

import com.mx.mbrl.security.JwtAuthFilter;
import com.mx.mbrl.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private JwtUtil jwtUtil;

	@Bean
	public JwtAuthFilter jwtAuthFilter() {
		return new JwtAuthFilter(jwtUtil, userDetailsService);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((req, res, e) -> {
					res.setContentType("application/json;charset=UTF-8");
					res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					res.getWriter().write("{\"success\":false,\"message\":\"Token inválido o no enviado. Inicia sesión en POST /api/auth/login\",\"status\":401}");
				})
				.accessDeniedHandler((req, res, e) -> {
					res.setContentType("application/json;charset=UTF-8");
					res.setStatus(HttpServletResponse.SC_FORBIDDEN);
					res.getWriter().write("{\"success\":false,\"message\":\"No tienes permisos para esta acción.\",\"status\":403}");
				})
			)
			.authorizeHttpRequests(authz -> authz
				// ── Públicos (sin JWT) ──────────────────────────────────
				.requestMatchers(HttpMethod.POST,    "/api/auth/register").permitAll()
				.requestMatchers(HttpMethod.POST,    "/api/auth/login").permitAll()
				.requestMatchers(HttpMethod.POST,    "/api/auth/forgot-password").permitAll()
				.requestMatchers(HttpMethod.GET,     "/api/auth/validate-reset-token").permitAll()
				.requestMatchers(HttpMethod.POST,    "/api/auth/reset-password").permitAll()
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers(HttpMethod.GET,     "/uploads/**").permitAll()
				.requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
				.requestMatchers("/actuator/health").permitAll()
				// ── Protegidos (requieren Authorization: Bearer <token>) ─
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration cfg = new CorsConfiguration();
		cfg.setAllowedOrigins(Arrays.asList(
			"http://localhost:3000",
			"http://localhost:4200",
			"http://localhost:5173",
			"http://localhost:5174",
			"http://127.0.0.1:3000",
			"http://127.0.0.1:4200",
			"http://127.0.0.1:5173",
			"http://127.0.0.1:5174"
		));
		cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		cfg.setAllowedHeaders(List.of("*"));
		cfg.setAllowCredentials(true);
		cfg.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cfg);
		return source;
	}
}

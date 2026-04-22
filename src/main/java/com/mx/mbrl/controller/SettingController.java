package com.mx.mbrl.controller;

import com.mx.mbrl.dto.ApiResponse;
import com.mx.mbrl.dto.HomeSettingsDTO;
import com.mx.mbrl.service.SettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingController {

	private final SettingService settingService;

	@GetMapping("/home")
	public ResponseEntity<ApiResponse<HomeSettingsDTO>> getHomeSettings() {
		log.info("GET /api/settings/home");
		HomeSettingsDTO data = settingService.getHomeSettings();
		return ResponseEntity.ok(ApiResponse.success(data, "Configuración obtenida"));
	}

	@PutMapping("/home")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<HomeSettingsDTO>> updateHomeSettings(
			@RequestBody HomeSettingsDTO dto) {
		log.info("PUT /api/settings/home");
		HomeSettingsDTO updated = settingService.updateHomeSettings(dto);
		return ResponseEntity.ok(ApiResponse.success(updated, "Configuración guardada"));
	}
}

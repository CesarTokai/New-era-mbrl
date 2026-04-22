package com.mx.mbrl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mx.mbrl.dto.HomeSettingsDTO;
import com.mx.mbrl.entity.Setting;
import com.mx.mbrl.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingService {

	private static final String HOME_KEY = "home";

	private final SettingRepository settingRepository;
	private final ObjectMapper objectMapper;

	@Transactional(readOnly = true)
	public HomeSettingsDTO getHomeSettings() {
		return settingRepository.findById(HOME_KEY)
				.map(s -> deserialize(s.getValue()))
				.orElseGet(this::emptyHomeSettings);
	}

	@Transactional
	public HomeSettingsDTO updateHomeSettings(HomeSettingsDTO dto) {
		String json = serialize(dto);
		Setting setting = settingRepository.findById(HOME_KEY)
				.orElse(new Setting(HOME_KEY, json, null));
		setting.setValue(json);
		settingRepository.save(setting);
		log.info("Home settings actualizados");
		return dto;
	}

	private HomeSettingsDTO deserialize(String json) {
		try {
			return objectMapper.readValue(json, HomeSettingsDTO.class);
		} catch (Exception e) {
			log.error("Error deserializando home settings: {}", e.getMessage());
			return emptyHomeSettings();
		}
	}

	private String serialize(HomeSettingsDTO dto) {
		try {
			return objectMapper.writeValueAsString(dto);
		} catch (Exception e) {
			throw new RuntimeException("Error serializando home settings", e);
		}
	}

	private HomeSettingsDTO emptyHomeSettings() {
		return new HomeSettingsDTO(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}
}

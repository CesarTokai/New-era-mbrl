package com.mx.mbrl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Value("${app.upload.dir:uploads/images}")
	private String uploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Sirve las imágenes subidas en /uploads/images/** como archivos estáticos
		Path uploadPath = Paths.get(uploadDir).toAbsolutePath();

		registry.addResourceHandler("/uploads/images/**")
				.addResourceLocations("file:" + uploadPath + "/");
	}
}


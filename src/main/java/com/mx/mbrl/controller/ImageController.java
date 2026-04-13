package com.mx.mbrl.controller;

import com.mx.mbrl.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/images")
public class ImageController {

	@Value("${app.upload.dir:uploads/images}")
	private String uploadDir;

	@Value("${app.base-url:http://localhost:8080}")
	private String baseUrl;

	@Value("${app.upload.max-size:5242880}")
	private long maxFileSize;

	private static final String[] ALLOWED_TYPES = {
		"image/jpeg", "image/png", "image/gif", "image/webp", "image/jpg"
	};

	@PostMapping("/upload")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<String>> uploadImage(
			@RequestParam("file") MultipartFile file) {

		log.info("Subiendo imagen: {}, tamaño: {} bytes", file.getOriginalFilename(), file.getSize());

		try {
			// Validar que no esté vacío
			if (file.isEmpty()) {
				return ResponseEntity.badRequest()
						.body(ApiResponse.error("El archivo está vacío", 400));
			}

			// Validar tipo de archivo
			String contentType = file.getContentType();
			boolean validType = false;
			for (String allowed : ALLOWED_TYPES) {
				if (allowed.equals(contentType)) {
					validType = true;
					break;
				}
			}
			if (!validType) {
				return ResponseEntity.badRequest()
						.body(ApiResponse.error("Solo se permiten imágenes JPG, PNG, GIF o WEBP", 400));
			}

			// Validar tamaño (máx 5MB)
			if (file.getSize() > maxFileSize) {
				return ResponseEntity.badRequest()
						.body(ApiResponse.error("El archivo excede el tamaño máximo de 5MB", 400));
			}

			// Crear directorio si no existe
			Path uploadPath = Paths.get(uploadDir);
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			// Generar nombre único para el archivo
			String extension = getExtension(file.getOriginalFilename());
			String uniqueFileName = UUID.randomUUID().toString() + extension;
			Path filePath = uploadPath.resolve(uniqueFileName);

			// Guardar archivo
			Files.copy(file.getInputStream(), filePath);
			log.info("Imagen guardada en: {}", filePath);

			// Construir URL pública
			String imageUrl = baseUrl + "/uploads/images/" + uniqueFileName;

			return ResponseEntity.status(HttpStatus.CREATED)
					.body(ApiResponse.success(imageUrl, "Imagen subida exitosamente"));

		} catch (IOException e) {
			log.error("Error guardando imagen: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error al guardar la imagen", 500));
		}
	}

	@DeleteMapping("/{fileName}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable String fileName) {
		log.info("Eliminando imagen: {}", fileName);

		try {
			// Sanitizar nombre para evitar path traversal
			if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
				return ResponseEntity.badRequest()
						.body(ApiResponse.error("Nombre de archivo inválido", 400));
			}

			Path filePath = Paths.get(uploadDir).resolve(fileName);
			if (Files.exists(filePath)) {
				Files.delete(filePath);
				log.info("Imagen eliminada: {}", filePath);
				return ResponseEntity.ok(ApiResponse.success(null, "Imagen eliminada exitosamente"));
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(ApiResponse.error("Imagen no encontrada", 404));
			}
		} catch (IOException e) {
			log.error("Error eliminando imagen: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(ApiResponse.error("Error al eliminar la imagen", 500));
		}
	}

	private String getExtension(String fileName) {
		if (fileName == null || !fileName.contains(".")) {
			return ".jpg";
		}
		return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
	}
}


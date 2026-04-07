package com.mx.mbrl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender javaMailSender;

	@Value("${spring.mail.from:noreply@mbrl.com}")
	private String fromEmail;

	@Value("${app.reset-password-url:http://localhost:3000/reset-password}")
	private String resetPasswordUrl;

	public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
		log.info("Enviando email de reset de contraseña a: {}", toEmail);

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromEmail);
			message.setTo(toEmail);
			message.setSubject("Restablecer contraseña - MBRL Tienda");

			String resetLink = resetPasswordUrl + "?token=" + resetToken;
			String emailBody = String.format(
					"Hola %s,\n\n" +
					"Recibimos una solicitud para restablecer tu contraseña.\n\n" +
					"Haz clic en el siguiente enlace para restablecer tu contraseña:\n" +
					"%s\n\n" +
					"Este enlace expirará en 24 horas.\n\n" +
					"Si no solicitaste este cambio, ignora este email.\n\n" +
					"Saludos,\n" +
					"Equipo MBRL",
					userName, resetLink
			);

			message.setText(emailBody);
			javaMailSender.send(message);

			log.info("Email de reset enviado exitosamente a: {}", toEmail);
		} catch (Exception e) {
			log.error("Error enviando email de reset a {}: {}", toEmail, e.getMessage());
			throw new RuntimeException("Error enviando email de reset", e);
		}
	}

	public void sendPasswordChangeNotification(String toEmail, String userName) {
		log.info("Enviando notificación de cambio de contraseña a: {}", toEmail);

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromEmail);
			message.setTo(toEmail);
			message.setSubject("Contraseña cambiada - MBRL Tienda");

			String emailBody = String.format(
					"Hola %s,\n\n" +
					"Tu contraseña ha sido cambiada exitosamente.\n\n" +
					"Si no realizaste este cambio, contacta a soporte inmediatamente.\n\n" +
					"Saludos,\n" +
					"Equipo MBRL",
					userName
			);

			message.setText(emailBody);
			javaMailSender.send(message);

			log.info("Notificación de cambio enviada a: {}", toEmail);
		} catch (Exception e) {
			log.error("Error enviando notificación a {}: {}", toEmail, e.getMessage());
			// No lanzar excepción - la contraseña ya cambió
		}
	}
}


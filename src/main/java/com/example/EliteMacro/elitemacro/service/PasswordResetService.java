package com.example.EliteMacro.elitemacro.service;

import com.example.EliteMacro.elitemacro.model.Usuario;
import com.example.EliteMacro.elitemacro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username:#{null}}")
    private String mailUsername;

    public boolean solicitarResetPassword(String email) {
        // Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null) {
            return false; // No revelar que el email no existe por seguridad
        }

        // Generar token único
        String token = UUID.randomUUID().toString();

        // Establecer token y fecha de expiración (1 hora)
        usuario.setResetPasswordToken(token);
        usuario.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        usuarioRepository.save(usuario);

        // Enviar email
        sendResetEmail(usuario.getEmail(), token, usuario.getUsername());

        return true;
    }

    public boolean validarToken(String email, String token) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null || usuario.getResetPasswordToken() == null) {
            return false;
        }

        // Verificar token y que no haya expirado
        return usuario.getResetPasswordToken().equals(token) &&
                usuario.getResetTokenExpiry().isAfter(LocalDateTime.now());
    }

    public boolean resetPassword(String email, String token, String newPassword) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null || !validarToken(email, token)) {
            return false;
        }

        // Actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(newPassword));

        // Limpiar token
        usuario.setResetPasswordToken(null);
        usuario.setResetTokenExpiry(null);

        usuarioRepository.save(usuario);

        return true;
    }

    private void sendResetEmail(String toEmail, String token, String username) {
        try {
            // Verificar si el mail está configurado
            if (mailSender == null || mailUsername == null || mailUsername.isEmpty()) {
                System.out.println("⚠️ Servicio de email NO configurado. Simulando envío a: " + toEmail);
                System.out.println("📧 Token para " + toEmail + " (" + username + "): " + token);
                System.out.println("🔗 Enlace simulado: http://tu-app.com/reset-password?token=" + token + "&email=" + toEmail);
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Restablecimiento de contraseña - EliteMacro");

            String resetLink = "http://tu-app.com/reset-password?token=" + token + "&email=" + toEmail;

            String emailContent = "Hola " + username + ",\n\n" +
                    "Has solicitado restablecer tu contraseña en EliteMacro.\n\n" +
                    "Para crear una nueva contraseña, haz clic en el siguiente enlace:\n" +
                    resetLink + "\n\n" +
                    "Este enlace expirará en 1 hora.\n\n" +
                    "Si no solicitaste este cambio, ignora este correo.\n\n" +
                    "Saludos,\n" +
                    "El equipo de EliteMacro";

            message.setText(emailContent);

            mailSender.send(message);
            System.out.println("✅ Email enviado exitosamente a: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Error enviando email a " + toEmail + ": " + e.getMessage());
        }
    }
}
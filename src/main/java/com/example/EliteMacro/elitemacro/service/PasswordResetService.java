package com.example.EliteMacro.elitemacro.service;

import com.example.EliteMacro.elitemacro.model.Usuario;
import com.example.EliteMacro.elitemacro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ========== MÉTODO 1: Solicitar reset ==========
    public boolean solicitarResetPassword(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null) {
            System.out.println("⚠️ Email no encontrado: " + email);
            return false;
        }

        String token = UUID.randomUUID().toString();
        System.out.println("🔑 Token generado: " + token);

        usuario.setResetPasswordToken(token);
        usuario.setResetTokenExpiry(LocalDateTime.now().plusHours(1));

        try {
            usuarioRepository.save(usuario);
            System.out.println("✅ Token guardado para: " + usuario.getUsername());
        } catch (Exception e) {
            System.err.println("❌ Error guardando token: " + e.getMessage());
            return false;
        }

        sendResetEmail(usuario.getEmail(), token, usuario.getUsername());
        return true;
    }

    // ========== MÉTODO 2: Validar token ==========
    public boolean validarToken(String email, String token) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null || usuario.getResetPasswordToken() == null) {
            System.out.println("❌ Token inválido - Usuario o token nulo");
            return false;
        }

        boolean esValido = usuario.getResetPasswordToken().equals(token) &&
                usuario.getResetTokenExpiry().isAfter(LocalDateTime.now());

        System.out.println("🔍 Validación token " + token + ": " + (esValido ? "VÁLIDO" : "INVÁLido/EXPIRO"));
        return esValido;
    }

    // ========== MÉTODO 3: Resetear contraseña ==========
    public boolean resetPassword(String email, String token, String newPassword) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null || !validarToken(email, token)) {
            System.out.println("❌ No se puede resetear - Token inválido");
            return false;
        }

        try {
            usuario.setPassword(passwordEncoder.encode(newPassword));
            usuario.setResetPasswordToken(null);
            usuario.setResetTokenExpiry(null);
            usuarioRepository.save(usuario);
            System.out.println("✅ Contraseña actualizada para: " + email);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error actualizando contraseña: " + e.getMessage());
            return false;
        }
    }

    // ========== MÉTODO 4: Enviar email ==========
    private void sendResetEmail(String toEmail, String token, String username) {
        try {
            // ENLACE REAL para tu app en Render
            String resetLink = "https://elitemacro.onrender.com/reset-password.html?token=" +
                    token + "&email=" + toEmail;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("samlol25d@gmail.com"); // ¡IMPORTANTE!
            message.setTo(toEmail);
            message.setSubject("Restablecimiento de contraseña - EliteMacro");

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
            System.out.println("✅ Email REAL enviado a: " + toEmail);
            System.out.println("🔗 Enlace enviado: " + resetLink);

        } catch (Exception e) {
            System.err.println("❌ Error REAL enviando email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
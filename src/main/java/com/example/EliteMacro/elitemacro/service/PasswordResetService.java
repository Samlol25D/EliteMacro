package com.example.EliteMacro.elitemacro.service;

import com.example.EliteMacro.elitemacro.model.Usuario;
import com.example.EliteMacro.elitemacro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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

    @Autowired
    private ApplicationContext context; // Spring ApplicationContext, NO Apache Tomcat

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ========== MÉTODO 1: Solicitar reset ==========
    public boolean solicitarResetPassword(String email) {
        System.out.println("=== SOLICITANDO RESET PARA: " + email + " ===");

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null) {
            System.out.println("⚠️ Email no encontrado: " + email);
            return false; // Por seguridad, pero podrías devolver true
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

        // Obtener JavaMailSender y enviar email
        JavaMailSender mailSender = getMailSender();
        if (mailSender == null) {
            System.err.println("❌ No se puede enviar email - JavaMailSender es NULL");
            return false;
        }

        sendResetEmail(mailSender, usuario.getEmail(), token, usuario.getUsername());
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

        System.out.println("🔍 Validación token " + token + ": " + (esValido ? "VÁLIDO" : "INVÁLIDO/EXPIRÓ"));
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

    // ========== MÉTODO AUXILIAR: Obtener JavaMailSender ==========
    private JavaMailSender getMailSender() {
        try {
            // CORRECCIÓN: Usar getBean() en lugar de getRealPath()
            JavaMailSender mailSender = context.getBean(JavaMailSender.class);
            System.out.println("✅ JavaMailSender obtenido correctamente");
            return mailSender;
        } catch (Exception e) {
            System.err.println("❌ ERROR: No se pudo obtener JavaMailSender");
            System.err.println("   Razón: " + e.getMessage());
            System.err.println("   Verifica que tengas la configuración de correo en application.yml");
            return null;
        }
    }

    // ========== MÉTODO 4: Enviar email ==========
    private void sendResetEmail(JavaMailSender mailSender, String toEmail, String token, String username) {
        try {
            System.out.println("=== ENVIANDO EMAIL REAL ===");
            System.out.println("Para: " + toEmail);
            System.out.println("Usuario: " + username);
            System.out.println("MailSender disponible: " + (mailSender != null));

            if (mailSender == null) {
                throw new RuntimeException("JavaMailSender es NULL");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("samlol25d@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Restablecimiento de contraseña - EliteMacro");

            String resetLink = "https://elitemacro.onrender.com/reset-password.html?token=" +
                    token + "&email=" + toEmail;

            String emailContent = "Hola " + username + ",\n\n" +
                    "Has solicitado restablecer tu contraseña en EliteMacro.\n\n" +
                    "Para crear una nueva contraseña, haz clic en el siguiente enlace:\n" +
                    resetLink + "\n\n" +
                    "Este enlace expirará en 1 hora.\n\n" +
                    "Si no solicitaste este cambio, ignora este correo.\n\n" +
                    "Saludos,\n" +
                    "El equipo de EliteMacro";

            message.setText(emailContent);

            // INTENTAR enviar
            mailSender.send(message);
            System.out.println("✅ ¡EMAIL REAL ENVIADO EXITOSAMENTE!");
            System.out.println("🔗 Enlace: " + resetLink);

        } catch (Exception e) {
            System.err.println("❌ ERROR REAL enviando email: " + e.getMessage());
            e.printStackTrace();

            // Mostrar ayuda específica
            if (e.getMessage() != null) {
                if (e.getMessage().contains("535") || e.getMessage().contains("Invalid login")) {
                    System.err.println("⚠️  ERROR 535: Contraseña incorrecta");
                    System.err.println("   Usa una CONTRASEÑA DE APLICACIÓN de Gmail");
                    System.err.println("   Ve a: https://myaccount.google.com/apppasswords");
                } else if (e.getMessage().contains("Could not connect") || e.getMessage().contains("connect timed out")) {
                    System.err.println("⚠️  Error de conexión");
                    System.err.println("   Verifica tu conexión a internet o firewall");
                } else if (e.getMessage().contains("NULL")) {
                    System.err.println("⚠️  JavaMailSender no está configurado");
                    System.err.println("   Crea un archivo MailConfig.java o configura application.yml");
                }
            }
        }
    }
}
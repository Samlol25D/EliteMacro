package com.example.EliteMacro.elitemacro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        System.out.println("=== CONFIGURANDO JavaMailSender MANUALMENTE ===");

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // Configuración de Gmail
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("samlol25d@gmail.com");
        mailSender.setPassword("sadpsnxmsoyaqufe"); // SIN espacios

        // Propiedades de JavaMail
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "true"); // Para ver logs
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        System.out.println("✅ JavaMailSender configurado para: " + mailSender.getUsername());
        return mailSender;
    }
}
package com.example.EliteMacro.elitemacro.controller;

import com.example.EliteMacro.elitemacro.model.Usuario;
import com.example.EliteMacro.elitemacro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller  // Cambia de @RestController a @Controller
@RequestMapping("/api/registro")
public class RegistroController {

    private static final Logger logger = LoggerFactory.getLogger(RegistroController.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public String registrar(@ModelAttribute Usuario usuario) {  // Cambia a @ModelAttribute
        logger.info("Intentando registrar usuario: {}", usuario.getUsername());

        try {
            // Validaciones básicas
            if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
                logger.warn("Username vacío recibido");
                return "redirect:/registro?error=username_required";
            }

            if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
                logger.warn("Password vacío recibido");
                return "redirect:/registro?error=password_required";
            }

            if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
                logger.warn("Email vacío recibido");
                return "redirect:/registro?error=email_required";
            }

            // Verificar si el usuario ya existe
            if (usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) {
                logger.warn("Usuario ya existe: {}", usuario.getUsername());
                return "redirect:/registro?error=user_exists";
            }

            // Verificar si el email ya existe
            if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
                logger.warn("Email ya existe: {}", usuario.getEmail());
                return "redirect:/registro?error=email_exists";
            }

            // Preparar usuario para guardar
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            usuario.setRol("ROLE_USER");
            usuario.setActivo(true);

            // Guardar usuario
            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            logger.info("Usuario guardado exitosamente con ID: {}", usuarioGuardado.getId());

            return "redirect:/login?success";  // Redirige a login después del registro

        } catch (Exception e) {
            logger.error("Error al registrar usuario: {}", e.getMessage(), e);
            return "redirect:/registro?error=internal_error";
        }
    }
}
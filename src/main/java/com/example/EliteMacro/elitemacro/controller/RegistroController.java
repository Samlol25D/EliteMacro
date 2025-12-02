package com.example.EliteMacro.elitemacro.controller;

import com.example.EliteMacro.elitemacro.model.Usuario;
import com.example.EliteMacro.elitemacro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Controller
@RequestMapping("/api/registro")
public class RegistroController {

    private static final Logger logger = LoggerFactory.getLogger(RegistroController.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public String registrar(@ModelAttribute Usuario usuario,
                            RedirectAttributes redirectAttributes) {  // Añade RedirectAttributes
        logger.info("Intentando registrar usuario: {}", usuario.getUsername());

        try {
            // Validaciones básicas
            if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
                logger.warn("Username vacío recibido");
                redirectAttributes.addFlashAttribute("error", "username_required");
                redirectAttributes.addFlashAttribute("username", usuario.getUsername());
                redirectAttributes.addFlashAttribute("email", usuario.getEmail());
                return "redirect:/registro";
            }

            if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
                logger.warn("Password vacío recibido");
                redirectAttributes.addFlashAttribute("error", "password_required");
                redirectAttributes.addFlashAttribute("username", usuario.getUsername());
                redirectAttributes.addFlashAttribute("email", usuario.getEmail());
                return "redirect:/registro";
            }

            if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
                logger.warn("Email vacío recibido");
                redirectAttributes.addFlashAttribute("error", "email_required");
                redirectAttributes.addFlashAttribute("username", usuario.getUsername());
                redirectAttributes.addFlashAttribute("email", usuario.getEmail());
                return "redirect:/registro";
            }

            // Validar contraseña
            if (!validarPassword(usuario.getPassword())) {
                logger.warn("Contraseña no válida");
                redirectAttributes.addFlashAttribute("error", "password_invalid");
                redirectAttributes.addFlashAttribute("username", usuario.getUsername());
                redirectAttributes.addFlashAttribute("email", usuario.getEmail());
                return "redirect:/registro";
            }

            // Verificar si el usuario ya existe
            if (usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) {
                logger.warn("Usuario ya existe: {}", usuario.getUsername());
                redirectAttributes.addFlashAttribute("error", "user_exists");
                redirectAttributes.addFlashAttribute("username", usuario.getUsername());
                redirectAttributes.addFlashAttribute("email", usuario.getEmail());
                return "redirect:/registro";
            }

            // Verificar si el email ya existe
            if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
                logger.warn("Email ya existe: {}", usuario.getEmail());
                redirectAttributes.addFlashAttribute("error", "email_exists");
                redirectAttributes.addFlashAttribute("username", usuario.getUsername());
                redirectAttributes.addFlashAttribute("email", usuario.getEmail());
                return "redirect:/registro";
            }

            // Preparar usuario para guardar
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            usuario.setRol("ROLE_USER");
            usuario.setActivo(true);

            // Guardar usuario
            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            logger.info("Usuario guardado exitosamente con ID: {}", usuarioGuardado.getId());

            redirectAttributes.addFlashAttribute("success", "Registro exitoso");
            return "redirect:/login";

        } catch (Exception e) {
            logger.error("Error al registrar usuario: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "internal_error");
            redirectAttributes.addFlashAttribute("username", usuario.getUsername());
            redirectAttributes.addFlashAttribute("email", usuario.getEmail());
            return "redirect:/registro";
        }
    }

    private boolean validarPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        // Validar mayúscula
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // Validar carácter especial
        if (!password.matches(".*[@$!%*?&].*")) {
            return false;
        }

        return true;
    }

    // Endpoint para verificar disponibilidad de username (opcional)
    @GetMapping("/check-username")
    @ResponseBody
    public Map<String, Boolean> checkUsername(@RequestParam String username) {
        boolean exists = usuarioRepository.findByUsername(username).isPresent();
        return Map.of("exists", exists);
    }
}
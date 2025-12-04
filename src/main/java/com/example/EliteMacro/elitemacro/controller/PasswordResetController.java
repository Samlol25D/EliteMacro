package com.example.EliteMacro.elitemacro.controller;

import com.example.EliteMacro.elitemacro.dto.PasswordResetRequest;
import com.example.EliteMacro.elitemacro.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/password")
@CrossOrigin(origins = "*")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    // Solicitar reset de contraseña
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        boolean success = passwordResetService.solicitarResetPassword(email);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ?
                "Se ha enviado un enlace de recuperación a tu correo." :
                "Si el correo existe, recibirás un enlace de recuperación.");

        return ResponseEntity.ok(response);
    }

    // Validar token
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody PasswordResetRequest request) {
        boolean isValid = passwordResetService.validarToken(request.getEmail(), request.getToken());

        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);

        return ResponseEntity.ok(response);
    }

    // Resetear contraseña
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        boolean success = passwordResetService.resetPassword(
                request.getEmail(),
                request.getToken(),
                request.getNewPassword()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ?
                "Contraseña actualizada exitosamente." :
                "Token inválido o expirado.");

        return ResponseEntity.ok(response);
    }
}
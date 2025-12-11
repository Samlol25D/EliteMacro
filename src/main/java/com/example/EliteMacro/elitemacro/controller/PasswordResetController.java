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

        // Validación básica
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("El email es requerido"));
        }

        System.out.println("📧 Solicitud de reset para: " + email);

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
        // Validaciones
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("El email es requerido"));
        }

        if (request.getToken() == null || request.getToken().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("El token es requerido"));
        }

        System.out.println("🔍 Validando token para: " + request.getEmail());

        boolean isValid = passwordResetService.validarToken(request.getEmail(), request.getToken());

        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        response.put("message", isValid ?
                "Token válido" : "Token inválido o expirado");

        return ResponseEntity.ok(response);
    }

    // Resetear contraseña
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        // Validaciones
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("El email es requerido"));
        }

        if (request.getToken() == null || request.getToken().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("El token es requerido"));
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("La nueva contraseña es requerida"));
        }

        if (request.getNewPassword().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("La contraseña debe tener al menos 6 caracteres"));
        }

        System.out.println("🔄 Reseteando contraseña para: " + request.getEmail());

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

    // Método auxiliar para crear respuestas de error
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        return errorResponse;
    }
}
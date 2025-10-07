package com.example.EliteMacro.elitemacro.controller;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    @GetMapping("/usuario")
    public String getUsuarioActual(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            // Solo devolver el nombre de usuario
            return auth.getName();
        }
        return "Invocador";
    }

    @GetMapping("/usuario-info")
    public ResponseEntity<?> getUsuarioInfo(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", auth.getName());
            userInfo.put("roles", auth.getAuthorities().toString());
            userInfo.put("isAdmin", auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            return ResponseEntity.ok(userInfo);
        }
        return ResponseEntity.status(401).build();
    }
}
package com.example.EliteMacro.elitemacro.controller;

import jakarta.persistence.Column;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    @Column(nullable = false)
    private boolean activo = true;

    @GetMapping("/usuario")
    public String getUsuarioActual(Authentication auth) {
        return auth.getName() + " (" + auth.getAuthorities() + ")";
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

}

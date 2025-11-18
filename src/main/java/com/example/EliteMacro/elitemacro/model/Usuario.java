package com.example.EliteMacro.elitemacro.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String rol = "ROLE_USER";

    @Column(nullable = false)
    private boolean activo = true;

    @Column(unique = true, nullable = false)
    private String email;

    // Nuevos campos para el sistema de niveles
    @Column(name = "experiencia_total", nullable = false)
    private int experienciaTotal = 0;

    @Column(name = "experiencia_actual", nullable = false)
    private int experienciaActual = 0;

    @Column(nullable = false)
    private int nivel = 1;

    @Column(name = "experiencia_para_siguiente_nivel", nullable = false)
    private int experienciaParaSiguienteNivel = 100;

    // Constructores
    public Usuario() {}

    public Usuario(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.experienciaTotal = 0;
        this.experienciaActual = 0;
        this.nivel = 1;
        this.experienciaParaSiguienteNivel = calcularExperienciaParaSiguienteNivel(1);
    }

    // Getters y setters existentes
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Override
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @Override
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Getters y setters para el sistema de niveles
    public int getExperienciaTotal() { return experienciaTotal; }
    public void setExperienciaTotal(int experienciaTotal) { this.experienciaTotal = experienciaTotal; }

    public int getExperienciaActual() { return experienciaActual; }
    public void setExperienciaActual(int experienciaActual) { this.experienciaActual = experienciaActual; }

    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }

    public int getExperienciaParaSiguienteNivel() { return experienciaParaSiguienteNivel; }
    public void setExperienciaParaSiguienteNivel(int experienciaParaSiguienteNivel) {
        this.experienciaParaSiguienteNivel = experienciaParaSiguienteNivel;
    }

    // Métodos para el sistema de niveles
    public int calcularExperienciaParaSiguienteNivel(int nivel) {
        return (int) (100 * Math.pow(nivel, 1.5));
    }

    public void agregarExperiencia(int experiencia) {
        this.experienciaTotal += experiencia;
        this.experienciaActual += experiencia;

        // Verificar si subió de nivel
        while (this.experienciaActual >= this.experienciaParaSiguienteNivel) {
            subirNivel();
        }
    }

    public void subirNivel() {
        this.experienciaActual -= this.experienciaParaSiguienteNivel;
        this.nivel++;
        this.experienciaParaSiguienteNivel = calcularExperienciaParaSiguienteNivel(this.nivel);
    }

    public double getPorcentajeProgreso() {
        return (double) this.experienciaActual / this.experienciaParaSiguienteNivel * 100;
    }

    public String getRango() {
        if (nivel >= 50) return "CHALLENGER";
        if (nivel >= 40) return "GRANMASTER";
        if (nivel >= 30) return "MASTER";
        if (nivel >= 20) return "DIAMANTE";
        if (nivel >= 15) return "PLATINO";
        if (nivel >= 10) return "ORO";
        if (nivel >= 5) return "PLATA";
        if (nivel >= 3) return "BRONCE";
        return "HIERRO";
    }

    // Métodos de UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(rol));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
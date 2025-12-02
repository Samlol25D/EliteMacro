package com.example.EliteMacro.elitemacro.controller;

import com.example.EliteMacro.elitemacro.model.Usuario;
import com.example.EliteMacro.elitemacro.repository.HabitoRepository;
import com.example.EliteMacro.elitemacro.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UsuarioRepository usuarioRepository;

    @Autowired
    protected HabitoRepository habitoRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected Usuario usuarioTest;
    protected Usuario adminTest;

    @BeforeEach
    void setUpBase() {
        System.out.println("=== INICIANDO PRUEBA CON MYSQL ===");
        System.out.println("Base de datos: elitemacro_testdb");

        // IMPORTANTE: Eliminar en el orden correcto primero hábitos, luego usuarios
        habitoRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Crear usuario de prueba para League of Legends
        usuarioTest = crearUsuarioTest("midlaner123", "mid@elitemacro.com", "ROLE_USER");
        adminTest = crearUsuarioTest("adminchallenger", "admin@elitemacro.com", "ROLE_ADMIN");
    }

    @AfterEach
    void cleanUp() {
        System.out.println("=== FINALIZANDO PRUEBA ===");
    }

    protected Usuario crearUsuarioTest(String username, String email, String rol) {
        Usuario usuario = new Usuario(username, passwordEncoder.encode("password123"), email);
        usuario.setRol(rol);
        usuario.setActivo(true);

        // Simular progreso en League of Legends
        usuario.setNivel(generarNivelAleatorio());
        usuario.setExperienciaTotal(calcularExperienciaTotal(usuario.getNivel()));
        usuario.setExperienciaActual(generarExpActual());
        usuario.setExperienciaParaSiguienteNivel(calcularExpParaSiguienteNivel(usuario.getNivel()));

        return usuarioRepository.save(usuario);
    }

    protected int generarNivelAleatorio() {
        // Niveles típicos de League of Legends (1-100)
        return (int) (Math.random() * 50) + 1;
    }

    protected int calcularExperienciaTotal(int nivel) {
        // Fórmula de experiencia para League of Legends
        return nivel * 100 + (nivel * nivel * 10);
    }

    protected int calcularExpParaSiguienteNivel(int nivel) {
        // Experiencia necesaria para subir de nivel
        return (int) (100 * Math.pow(nivel, 1.5));
    }

    protected int generarExpActual() {
        return (int) (Math.random() * 100) + 1;
    }

    protected String getRangoPorNivel(int nivel) {
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
}
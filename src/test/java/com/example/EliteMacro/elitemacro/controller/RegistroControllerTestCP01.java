package com.example.EliteMacro.elitemacro.controller;

import com.example.EliteMacro.elitemacro.controller.BaseTest;
import com.example.EliteMacro.elitemacro.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("CP01 - Registro exitoso de nuevo invocador")
class CP01Test extends BaseTest {

    @Test
    @DisplayName("CP01 - Registrar nuevo invocador exitosamente")
    void testCP01_RegistroExitoso() throws Exception {
        System.out.println("=== CP01: Registro exitoso (HU-01) ===");
        System.out.println("Descripción: Usuario no registrado crea cuenta nueva");

        // Usar FORM URL ENCODED en lugar de JSON porque el controller usa @ModelAttribute
        MvcResult result = mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "nuevoInvocador")
                        .param("password", "Password123")
                        .param("email", "nuevo@elitemacro.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?success"))
                .andReturn();

        // Verificar redirección a login
        String redirectedUrl = result.getResponse().getRedirectedUrl();
        assertNotNull(redirectedUrl);
        assertEquals("/login?success", redirectedUrl);

        // Verificar que el usuario se guardó en la BD
        Usuario usuario = usuarioRepository.findByUsername("nuevoInvocador").orElse(null);
        assertNotNull(usuario);
        assertEquals("nuevo@elitemacro.com", usuario.getEmail());
        assertEquals("ROLE_USER", usuario.getRol());
        assertTrue(usuario.isActivo());
        assertTrue(passwordEncoder.matches("Password123", usuario.getPassword()));

        // Verificar sistema de niveles de League of Legends
        assertEquals(1, usuario.getNivel(), "Debe empezar en nivel 1");
        assertEquals(0, usuario.getExperienciaTotal(), "Experiencia inicial debe ser 0");
        assertEquals(0, usuario.getExperienciaActual(), "Experiencia actual debe ser 0");
        assertTrue(usuario.getExperienciaParaSiguienteNivel() > 0, "Debe tener experiencia requerida para siguiente nivel");

        System.out.println("✅ Caso de prueba CP01 EXITOSO");
        System.out.println("   - Usuario registrado: " + usuario.getUsername());
        System.out.println("   - Email: " + usuario.getEmail());
        System.out.println("   - Nivel inicial: " + usuario.getNivel());
        System.out.println("   - Rango inicial: " + usuario.getRango()); // Hierro
        System.out.println("   - Redirigido a: " + redirectedUrl);
    }

    @Test
    @DisplayName("CP01 - Prevenir registro con nombre de invocador duplicado")
    void testRegistroUsuarioDuplicado() throws Exception {
        System.out.println("=== Test: Prevenir registro duplicado ===");

        // Crear usuario existente
        Usuario usuarioExistente = crearUsuarioTest("invocadorPro", "pro@elitemacro.com", "ROLE_USER");

        // Intentar registrar mismo nombre de invocador
        mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "invocadorPro")
                        .param("password", "Password123")
                        .param("email", "nuevoemail@elitemacro.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro?error=user_exists"));

        System.out.println("✅ Correctamente prevenido registro duplicado");
    }

    @Test
    @DisplayName("CP01 - Prevenir registro con email duplicado")
    void testRegistroEmailDuplicado() throws Exception {
        System.out.println("=== Test: Prevenir email duplicado ===");

        // Crear usuario con email
        Usuario usuarioExistente = crearUsuarioTest("summoner1", "summoner@elitemacro.com", "ROLE_USER");

        // Intentar registrar mismo email
        mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "summoner2")
                        .param("password", "Password123")
                        .param("email", "summoner@elitemacro.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro?error=email_exists"));

        System.out.println("✅ Correctamente prevenido email duplicado");
    }

    @Test
    @DisplayName("CP01 - Validar campos vacíos")
    void testRegistroCamposVacios() throws Exception {
        System.out.println("=== Test: Validación campos vacíos ===");

        // Test 1: Username vacío
        mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "")
                        .param("password", "Password123")
                        .param("email", "test@elitemacro.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro?error=username_required"));

        // Test 2: Password vacío
        mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "testUser")
                        .param("password", "")
                        .param("email", "test@elitemacro.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro?error=password_required"));

        // Test 3: Email vacío
        mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "testUser")
                        .param("password", "Password123")
                        .param("email", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro?error=email_required"));

        System.out.println("✅ Validación de campos vacíos funciona correctamente");
    }

    @Test
    @DisplayName("CP01 - Registrar invocador con nombre temático de League")
    void testRegistroInvocadorTematico() throws Exception {
        System.out.println("=== Test: Registrar invocador temático ===");

        // Nombres de invocadores temáticos de League of Legends
        String[] nombresInvocadores = {
                "DariusMain99",
                "ZedAssassin",
                "AhriMidLane",
                "JhinADCPro",
                "ThreshSupport"
        };

        for (String nombre : nombresInvocadores) {
            String email = nombre.toLowerCase() + "@elitemacro.com";

            mockMvc.perform(post("/api/registro")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", nombre)
                            .param("password", "SecurePass123")
                            .param("email", email))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login?success"));

            // Verificar que se creó
            Usuario usuario = usuarioRepository.findByUsername(nombre).orElse(null);
            assertNotNull(usuario, "Debe crear usuario: " + nombre);
            assertEquals(email, usuario.getEmail());

            System.out.println("   - Invocador creado: " + nombre + " (Nivel: " + usuario.getNivel() + ")");
        }

        System.out.println("✅ Todos los invocadores temáticos registrados exitosamente");
    }
}
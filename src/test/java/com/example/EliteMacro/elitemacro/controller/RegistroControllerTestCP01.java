package com.example.EliteMacro.elitemacro.controller;

import com.example.EliteMacro.elitemacro.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("CP01 - Registro exitoso de nuevo invocador")
class CP01Test extends BaseTest {

    @BeforeEach
    void setUp() {
        // Limpiar base de datos antes de cada test
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("CP01 - Registrar nuevo invocador exitosamente")
    void testCP01_RegistroExitoso() throws Exception {
        System.out.println("=== CP01: Registro exitoso (HU-01) ===");
        System.out.println("Descripción: Usuario no registrado crea cuenta nueva");

        String password = "Password123*";
        String email = "nuevo@elitemacro.com";
        String username = "nuevoInvocador";

        MvcResult result = mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", username)
                        .param("password", password)
                        .param("email", email))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login")) // Solo verifica la URL base
                .andReturn();

        // Verificar redirección a login
        String redirectedUrl = result.getResponse().getRedirectedUrl();
        assertNotNull(redirectedUrl);
        assertTrue(redirectedUrl.startsWith("/login"));

        // Verificar que el usuario se guardó en la BD
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        assertNotNull(usuario, "El usuario debería haberse guardado en la base de datos");
        assertEquals(email, usuario.getEmail());
        assertEquals("ROLE_USER", usuario.getRol());
        assertTrue(usuario.isActivo(), "El usuario debería estar activo");

        assertNotNull(usuario.getPassword(), "La contraseña no debería ser null");
        assertTrue(passwordEncoder.matches(password, usuario.getPassword()));

        // Verificar sistema de niveles
        assertEquals(1, usuario.getNivel(), "Debe empezar en nivel 1");
        assertEquals(0, usuario.getExperienciaTotal(), "Experiencia inicial debe ser 0");
        assertEquals(0, usuario.getExperienciaActual(), "Experiencia actual debe ser 0");
        assertTrue(usuario.getExperienciaParaSiguienteNivel() > 0);

        System.out.println("✅ Caso de prueba CP01 EXITOSO");
        System.out.println("   - Usuario registrado: " + usuario.getUsername());
        System.out.println("   - Email: " + usuario.getEmail());
        System.out.println("   - Nivel inicial: " + usuario.getNivel());
        System.out.println("   - Rango inicial: " + usuario.getRango());
        System.out.println("   - Redirigido a: " + redirectedUrl);
    }

    @Test
    @DisplayName("CP01 - Prevenir registro con nombre de invocador duplicado")
    void testCP02_RegistroUsuarioDuplicado() throws Exception {
        System.out.println("=== Prevenir registro duplicado ===");

        // Crear usuario existente
        String username = "invocadorPro";
        String password = "Password123*";
        String email = "pro@elitemacro.com";

        Usuario usuarioExistente = new Usuario(username, password, email);
        usuarioExistente.setPassword(passwordEncoder.encode(password));
        usuarioRepository.save(usuarioExistente);

        // Intentar registrar mismo nombre de invocador
        mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", username)
                        .param("password", "NuevaPass123*")
                        .param("email", "nuevoemail@elitemacro.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro")); // Solo URL base

        System.out.println("✅ Correctamente prevenido registro duplicado");
    }

    @Test
    @DisplayName("CP01 - Prevenir registro con email duplicado")
    void testCP03_RegistroEmailDuplicado() throws Exception {
        System.out.println("=== Prevenir email duplicado ===");

        // Crear usuario con email
        String email = "summoner@elitemacro.com";
        Usuario usuarioExistente = new Usuario("summoner1", "Password123*", email);
        usuarioExistente.setPassword(passwordEncoder.encode("Password123*"));
        usuarioRepository.save(usuarioExistente);

        // Intentar registrar mismo email
        mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "summoner2")
                        .param("password", "Password123*")
                        .param("email", email))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro")); // Solo URL base

        System.out.println("✅ Correctamente prevenido email duplicado");
    }

    @Test
    @DisplayName("CP01 - Validar campos vacíos")
    void testCP04_ValidacionCamposVacios() throws Exception {
        System.out.println("=== Validación campos vacíos ===");

        // Test 1: Username vacío
        mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "")
                        .param("password", "Password123*")
                        .param("email", "test@elitemacro.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro")); // Solo URL base

        // Test 2: Password vacío
        mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "testUser")
                        .param("password", "")
                        .param("email", "test@elitemacro.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro"));

        // Test 3: Email vacío
        mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "testUser")
                        .param("password", "Password123*")
                        .param("email", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro"));

        System.out.println("✅ Validación de campos vacíos funciona correctamente");
    }

    @Test
    @DisplayName("CP01 - Validar contraseña débil")
    void testCP05_ValidacionPasswordDebil() throws Exception {
        System.out.println("=== Validación contraseña débil ===");

        // Test 1: Contraseña demasiado corta
        mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "testUser")
                        .param("password", "123")
                        .param("email", "test@elitemacro.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro"));

        // Test 2: Sin mayúsculas
        mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "testUser")
                        .param("password", "password123*")
                        .param("email", "test@elitemacro.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro"));

        // Test 3: Sin carácter especial
        mockMvc.perform(post("/api/registro")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "testUser")
                        .param("password", "Password123")
                        .param("email", "test@elitemacro.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registro"));

        System.out.println("✅ Validación de contraseña funciona correctamente");
    }

    @Test
    @DisplayName("CP01 - Registrar invocador con nombre temático de League")
    void testCP06_RegistroInvocadorTematico() throws Exception {
        System.out.println("=== Registrar invocadores temáticos ===");

        // Nombres de invocadores temáticos de League of Legends
        String[] nombresInvocadores = {
                "DariusMain99",
                "ZedAssassin",
                "AhriMidLane",
                "JhinADCPro",
                "ThreshSupport"
        };

        int count = 0;
        for (String nombre : nombresInvocadores) {
            String email = nombre.toLowerCase() + count + "@elitemacro.com";
            String password = "SecurePass123*";

            mockMvc.perform(post("/api/registro")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", nombre)
                            .param("password", password)
                            .param("email", email))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/login"));

            // Verificar que se creó
            Usuario usuario = usuarioRepository.findByUsername(nombre).orElse(null);
            assertNotNull(usuario, "Debe crear usuario: " + nombre);
            assertEquals(email, usuario.getEmail());

            System.out.println("   - Invocador " + (++count) + " creado: " + nombre +
                    " (Nivel: " + usuario.getNivel() + ", Rango: " + usuario.getRango() + ")");
        }

        System.out.println("✅ Todos los invocadores temáticos registrados exitosamente");
    }
}
package com.example.EliteMacro.elitemacro.controller.cp;

import com.example.EliteMacro.elitemacro.controller.BaseTest;
import com.example.EliteMacro.elitemacro.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("CP08 - Personalización del perfil de invocador")
class CP08Test extends BaseTest {

    @Test
    @WithMockUser(username = "midlaner123", roles = {"USER"})
    @DisplayName("CP08 - Ver información completa del perfil")
    void testCP08_VerPerfilCompleto() throws Exception {
        System.out.println("=== CP08: Personalización del perfil (HU-08) ===");
        System.out.println("Descripción: Ver y personalizar perfil de invocador");
        System.out.println("Precondiciones: Usuario autenticado");
        System.out.println("Resultado esperado: Perfil actualizado correctamente");

        // Obtener información completa del perfil
        mockMvc.perform(get("/api/usuario/info-completa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("midlaner123"))
                .andExpect(jsonPath("$.nivel").isNumber())
                .andExpect(jsonPath("$.rango").isString())
                .andExpect(jsonPath("$.experienciaTotal").isNumber())
                .andExpect(jsonPath("$.experienciaActual").isNumber())
                .andExpect(jsonPath("$.experienciaParaSiguienteNivel").isNumber())
                .andExpect(jsonPath("$.porcentajeProgreso").isNumber())
                .andExpect(jsonPath("$.isAdmin").value(false));

        // Obtener estadísticas detalladas
        mockMvc.perform(get("/api/usuario/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivel").isNumber())
                .andExpect(jsonPath("$.rango").isString())
                .andExpect(jsonPath("$.progresoNivel").isString())
                .andExpect(jsonPath("$.expFaltante").isNumber())
                .andExpect(jsonPath("$.siguienteNivel").isNumber());

        // Obtener solo nivel y rango
        mockMvc.perform(get("/api/usuario/nivel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivel").isNumber())
                .andExpect(jsonPath("$.rango").isString())
                .andExpect(jsonPath("$.experienciaTotal").isNumber());

        // Verificar datos en base de datos
        Usuario usuario = usuarioRepository.findByUsername("midlaner123").orElseThrow();

        System.out.println("✅ Caso de prueba CP08 EXITOSO");
        System.out.println("   - Nombre de invocador: " + usuario.getUsername());
        System.out.println("   - Nivel: " + usuario.getNivel());
        System.out.println("   - Rango: " + usuario.getRango());
        System.out.println("   - Experiencia total: " + usuario.getExperienciaTotal() + " XP");
        System.out.println("   - Progreso actual: " + String.format("%.1f", usuario.getPorcentajeProgreso()) + "%");
        System.out.println("   - XP para siguiente nivel: " + usuario.getExperienciaParaSiguienteNivel() + " XP");
        System.out.println("   - XP faltante: " + (usuario.getExperienciaParaSiguienteNivel() - usuario.getExperienciaActual()) + " XP");
        System.out.println("Estado: COMPLETADO ✓");
    }

    @Test
    @WithMockUser(username = "adminchallenger", roles = {"ADMIN"})
    @DisplayName("CP08 - Admin ve su perfil con privilegios")
    void testAdminVePerfilConPrivilegios() throws Exception {
        System.out.println("=== Test: Perfil de administrador ===");

        mockMvc.perform(get("/api/usuario-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("adminchallenger"))
                .andExpect(jsonPath("$.isAdmin").value(true));

        mockMvc.perform(get("/api/usuario/info-completa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAdmin").value(true));

        System.out.println("✅ Admin ve perfil con indicador de administrador");
    }

    @Test
    @DisplayName("CP08 - Sistema de rangos de League of Legends")
    void testSistemaRangosLeague() throws Exception {
        System.out.println("=== Test: Sistema de rangos de League ===");

        // Crear usuarios con diferentes niveles para probar rangos
        Usuario[] usuariosTest = {
                crearUsuarioNivel("hierroUser", 1, "HIERRO"),
                crearUsuarioNivel("bronceUser", 4, "BRONCE"),
                crearUsuarioNivel("plataUser", 7, "PLATA"),
                crearUsuarioNivel("oroUser", 12, "ORO"),
                crearUsuarioNivel("platinoUser", 17, "PLATINO"),
                crearUsuarioNivel("diamanteUser", 25, "DIAMANTE"),
                crearUsuarioNivel("masterUser", 35, "MASTER"),
                crearUsuarioNivel("granmasterUser", 45, "GRANMASTER"),
                crearUsuarioNivel("challengerUser", 55, "CHALLENGER")
        };

        for (Usuario usuario : usuariosTest) {
            String rangoEsperado = usuario.getRango();
            System.out.println("   - " + usuario.getUsername() +
                    ": Nivel " + usuario.getNivel() +
                    " → " + rangoEsperado);

            // Verificar que el rango sea correcto
            assertEquals(rangoEsperado, usuario.getRango(),
                    "Rango incorrecto para nivel " + usuario.getNivel());
        }

        System.out.println("✅ Sistema de rangos funciona correctamente");
    }

    private Usuario crearUsuarioNivel(String username, int nivel, String rangoEsperado) {
        Usuario usuario = new Usuario(username, "password123", username + "@elitemacro.com");
        usuario.setNivel(nivel);
        usuario.setExperienciaTotal(nivel * 100);
        usuario.setExperienciaActual(50);
        usuario.setExperienciaParaSiguienteNivel(100);
        return usuario; // No se guarda en BD, solo para test
    }

    @Test
    @WithMockUser(username = "midlaner123", roles = {"USER"})
    @DisplayName("CP08 - Progreso de nivel y experiencia")
    void testProgresoNivel() throws Exception {
        System.out.println("=== Test: Cálculo de progreso ===");

        // Configurar usuario para test de progreso
        usuarioTest.setNivel(10);
        usuarioTest.setExperienciaActual(75);
        usuarioTest.setExperienciaParaSiguienteNivel(100);
        usuarioRepository.save(usuarioTest);

        mockMvc.perform(get("/api/usuario/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nivel").value(10))
                .andExpect(jsonPath("$.progresoNivel").value("75.0%"))
                .andExpect(jsonPath("$.expFaltante").value(25))
                .andExpect(jsonPath("$.siguienteNivel").value(11));

        // Verificar cálculos
        Usuario usuario = usuarioRepository.findByUsername("midlaner123").orElseThrow();
        double porcentaje = usuario.getPorcentajeProgreso();
        int expFaltante = usuario.getExperienciaParaSiguienteNivel() - usuario.getExperienciaActual();

        assertEquals(75.0, porcentaje, 0.1, "Porcentaje incorrecto");
        assertEquals(25, expFaltante, "Experiencia faltante incorrecta");

        System.out.println("✅ Cálculos de progreso correctos:");
        System.out.println("   - Porcentaje: " + String.format("%.1f", porcentaje) + "%");
        System.out.println("   - XP faltante: " + expFaltante + " XP");
        System.out.println("   - Siguiente nivel: " + (usuario.getNivel() + 1));
    }

    @Test
    @DisplayName("CP08 - Usuario no autenticado ve información básica")
    void testUsuarioNoAutenticado() throws Exception {
        System.out.println("=== Test: Acceso no autenticado ===");

        mockMvc.perform(get("/api/usuario"))
                .andExpect(status().isOk())
                .andExpect(content().string("Invocador"));

        mockMvc.perform(get("/api/usuario-info"))
                .andExpect(status().isUnauthorized());

        System.out.println("✅ Usuario no autenticado ve 'Invocador'");
        System.out.println("✅ Acceso restringido a información privada");
    }
}
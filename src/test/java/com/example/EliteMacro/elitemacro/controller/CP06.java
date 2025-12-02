package com.example.EliteMacro.elitemacro.controller;

import com.example.EliteMacro.elitemacro.controller.BaseTest;
import com.example.EliteMacro.elitemacro.model.Habito;
import com.example.EliteMacro.elitemacro.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("CP06 - Marcar hábito como completado y ganar experiencia")
class CP06Test extends BaseTest {

    private Habito habitoMID;
    private Habito habitoJUNGLE;
    private Long habitoId;

    @BeforeEach
    void setUpHabitosParaCompletar() {
        // Crear hábitos para completar
        habitoMID = crearHabitoCompletable("Farmeo perfecto MID",
                "80+ minions a 10 minutos como MID", "MID", usuarioTest, 20);

        habitoJUNGLE = crearHabitoCompletable("Gank exitoso early",
                "Gank exitoso antes de nivel 6", "JUNGLE", adminTest, 25);

        habitoId = habitoMID.getId();
    }

    private Habito crearHabitoCompletable(String nombre, String descripcion, String rol,
                                          Usuario usuario, int puntos) {
        Habito habito = new Habito();
        habito.setNombre(nombre);
        habito.setDescripcion(descripcion);
        habito.setRol(rol);
        habito.setUsuario(usuario);
        habito.setDificultad("MEDIA");
        habito.setFrecuencia("DIARIA");
        habito.setPuntosExperiencia(puntos);
        habito.setCompletado(false);
        habito.setExperienciaOtorgada(false);
        return habitoRepository.save(habito);
    }

    @Test
    @WithMockUser(username = "midlaner123", roles = {"USER"})
    @DisplayName("CP06 - Marcar hábito como completado primera vez")
    void testCP06_MarcarHabitoCompletado() throws Exception {
        System.out.println("=== CP06: Marcar hábito como completado (HU-06) ===");
        System.out.println("Descripción: Invocador completa hábito y gana experiencia");
        System.out.println("Precondiciones: Hábito existente no completado");
        System.out.println("Resultado esperado: Hábito marcado como completado, experiencia otorgada");

        // Obtener experiencia inicial del usuario
        int experienciaInicial = usuarioTest.getExperienciaTotal();
        int nivelInicial = usuarioTest.getNivel();

        System.out.println("   - Experiencia inicial: " + experienciaInicial + " XP");
        System.out.println("   - Nivel inicial: " + nivelInicial + " (" + usuarioTest.getRango() + ")");

        // Marcar hábito como completado
        String updateData = """
            {
                "completado": true
            }
            """;

        mockMvc.perform(put("/api/habitos/{id}", habitoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateData))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completado").value(true))
                .andExpect(jsonPath("$.experienciaOtorgada").value(true))
                .andExpect(jsonPath("$.nombre").value("Farmeo perfecto MID"));

        // Verificar en base de datos
        Habito habitoActualizado = habitoRepository.findById(habitoId).orElseThrow();
        assertTrue(habitoActualizado.isCompletado());
        assertTrue(habitoActualizado.isExperienciaOtorgada());

        // Verificar que el usuario ganó experiencia
        Usuario usuarioActualizado = usuarioRepository.findById(usuarioTest.getId()).orElseThrow();
        int experienciaGanada = 20; // Puntos del hábito
        int experienciaEsperada = experienciaInicial + experienciaGanada;

        assertEquals(experienciaEsperada, usuarioActualizado.getExperienciaTotal(),
                "El usuario debería haber ganado " + experienciaGanada + " XP");

        System.out.println("✅ Caso de prueba CP06 EXITOSO");
        System.out.println("   - Hábito completado: Farmeo perfecto MID");
        System.out.println("   - Experiencia otorgada: " + experienciaGanada + " XP");
        System.out.println("   - Experiencia total: " + usuarioActualizado.getExperienciaTotal() + " XP");
        System.out.println("   - Nivel actual: " + usuarioActualizado.getNivel() + " (" + usuarioActualizado.getRango() + ")");
        System.out.println("   - Progreso: " + String.format("%.1f", usuarioActualizado.getPorcentajeProgreso()) + "%");
        System.out.println("Estado: COMPLETADO ✓");
    }

    @Test
    @WithMockUser(username = "midlaner123", roles = {"USER"})
    @DisplayName("CP06 - Marcar hábito completado y verificar subida de nivel")
    void testCompletarHabitoSubidaNivel() throws Exception {
        System.out.println("=== Test: Completar hábito y subir de nivel ===");

        // Configurar usuario cerca de subir de nivel
        usuarioTest.setExperienciaActual(90); // Casi lleno
        usuarioTest.setExperienciaParaSiguienteNivel(100);
        usuarioRepository.save(usuarioTest);

        int nivelInicial = usuarioTest.getNivel();
        System.out.println("   - Nivel inicial: " + nivelInicial);

        // Completar hábito que dé suficiente experiencia para subir de nivel
        String updateData = """
            {
                "completado": true
            }
            """;

        mockMvc.perform(put("/api/habitos/{id}", habitoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateData))
                .andExpect(status().isOk());

        // Verificar subida de nivel
        Usuario usuarioActualizado = usuarioRepository.findById(usuarioTest.getId()).orElseThrow();
        assertTrue(usuarioActualizado.getNivel() > nivelInicial, "Debería haber subido de nivel");

        System.out.println("✅ Usuario subió de nivel!");
        System.out.println("   - Nivel nuevo: " + usuarioActualizado.getNivel());
        System.out.println("   - Rango nuevo: " + usuarioActualizado.getRango());
    }

    @Test
    @WithMockUser(username = "midlaner123", roles = {"USER"})
    @DisplayName("CP06 - No otorgar experiencia al recompletar hábito")
    void testNoExperienciaAlRecompletar() throws Exception {
        System.out.println("=== Test: No dar experiencia duplicada ===");

        // Primera vez - marcar como completado
        String updateData = """
            {
                "completado": true
            }
            """;

        mockMvc.perform(put("/api/habitos/{id}", habitoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateData));

        // Obtener experiencia después de primera vez
        Usuario usuarioDespuesPrimera = usuarioRepository.findById(usuarioTest.getId()).orElseThrow();
        int experienciaPrimera = usuarioDespuesPrimera.getExperienciaTotal();

        // Desmarcar y volver a marcar
        mockMvc.perform(put("/api/habitos/{id}", habitoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"completado\": false}"));

        mockMvc.perform(put("/api/habitos/{id}", habitoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateData));

        // Verificar que no se dio experiencia adicional
        Usuario usuarioDespuesSegunda = usuarioRepository.findById(usuarioTest.getId()).orElseThrow();
        assertEquals(experienciaPrimera, usuarioDespuesSegunda.getExperienciaTotal(),
                "No debería dar experiencia duplicada");

        System.out.println("✅ Correctamente no otorga experiencia duplicada");
    }

    @Test
    @WithMockUser(username = "adminchallenger", roles = {"ADMIN"})
    @DisplayName("CP06 - Admin puede marcar hábitos de otros usuarios")
    void testAdminPuedeMarcarHabitosDeOtros() throws Exception {
        System.out.println("=== Test: Admin completa hábito de otro usuario ===");

        // Admin marca hábito del usuario normal como completado
        String updateData = """
            {
                "completado": true,
                "nombre": "Actualizado por admin"
            }
            """;

        mockMvc.perform(put("/api/habitos/{id}", habitoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateData))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Actualizado por admin"));

        System.out.println("✅ Admin puede editar hábitos de otros usuarios");
    }

    @Test
    @WithMockUser(username = "midlaner123", roles = {"USER"})
    @DisplayName("CP06 - Usuario no puede marcar hábitos de otros usuarios")
    void testUsuarioNoPuedeMarcarHabitosDeOtros() throws Exception {
        System.out.println("=== Test: Usuario intenta completar hábito ajeno ===");

        // Usuario normal intenta marcar hábito del admin
        String updateData = """
            {
                "completado": true
            }
            """;

        mockMvc.perform(put("/api/habitos/{id}", habitoJUNGLE.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateData))
                .andExpect(status().isForbidden());

        System.out.println("✅ Correctamente denegado acceso a hábitos ajenos");
    }
}
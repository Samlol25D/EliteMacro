package com.example.EliteMacro.elitemacro.controller;

import com.example.EliteMacro.elitemacro.controller.BaseTest;
import com.example.EliteMacro.elitemacro.model.Habito;
import com.example.EliteMacro.elitemacro.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("CP05 - Filtro de hábitos por rol en League of Legends")
class CP05Test extends BaseTest {

    @BeforeEach
    void setUpHabitosPorRol() {
        // Limpiar hábitos existentes
        habitoRepository.deleteAll();

        // Crear hábitos para diferentes roles de League of Legends
        crearHabitoPorRol("Split push controlado", "Split push con visión y escape", "TOPLANE", usuarioTest);
        crearHabitoPorRol("Timer de objetivos", "Anotar Dragón/Barón timers", "JUNGLE", usuarioTest);
        crearHabitoPorRol("Roaming después de push", "Gank side lanes después de clear", "MID", usuarioTest);
        crearHabitoPorRol("Posicionamiento en teamfights", "Stay behind tanks", "ADC", adminTest);
        crearHabitoPorRol("Control de visión completo", "3+ wards en objetivos", "SUPPORT", adminTest);
        crearHabitoPorRol("Comunicación con pings", "Ping MIAs y objetivos", "TODOS", usuarioTest);
        crearHabitoPorRol("Farmeo temprano", "70+ CS a 10 minutos", "ADC", usuarioTest);
        crearHabitoPorRol("Peel para carry", "Proteger al ADC en fights", "SUPPORT", usuarioTest);
    }

    private void crearHabitoPorRol(String nombre, String descripcion, String rol, Usuario usuario) {
        Habito habito = new Habito();
        habito.setNombre(nombre);
        habito.setDescripcion(descripcion);
        habito.setRol(rol);
        habito.setUsuario(usuario);
        habito.setDificultad("MEDIA");
        habito.setFrecuencia("DIARIA");
        habito.setPuntosExperiencia(20);
        habito.setCompletado(false);
        habitoRepository.save(habito);
    }

    @Test
    @DisplayName("CP05 - Usuario sin rol específico ve solo hábitos TODOS")
    void testUsuarioSinRolEspecifico() throws Exception {
        System.out.println("=== Test: Usuario sin rol juego específico ===");

        // Crear usuario sin hábitos de rol específico
        Usuario usuarioSinRol = crearUsuarioTest("nuevoUser", "nuevo@elitemacro.com", "ROLE_USER");

        // Solo asignarle hábitos TODOS
        crearHabitoPorRol("Hábito para todos", "Este hábito es para todos", "TODOS", usuarioSinRol);

        mockMvc.perform(get("/api/habitos/mis-habitos")
                        .with(user("nuevoUser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rol").value("TODOS"));

        System.out.println("✅ Usuario sin rol específico:");
        System.out.println("   - Solo ve hábitos TODOS");
        System.out.println("   - Total hábitos: 1");
    }

    @Test
    @WithMockUser(username = "midlaner123", roles = {"USER"})
    @DisplayName("CP05 - Verificar estructura completa de hábitos filtrados")
    void testEstructuraCompletaHabitos() throws Exception {
        System.out.println("=== Test: Estructura de respuesta ===");

        mockMvc.perform(get("/api/habitos/mis-habitos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].nombre").isString())
                .andExpect(jsonPath("$[0].descripcion").isString())
                .andExpect(jsonPath("$[0].rol").isString())
                .andExpect(jsonPath("$[0].dificultad").isString())
                .andExpect(jsonPath("$[0].frecuencia").isString())
                .andExpect(jsonPath("$[0].puntosExperiencia").isNumber())
                .andExpect(jsonPath("$[0].completado").isBoolean())
                .andExpect(jsonPath("$[0].activo").isBoolean());

        System.out.println("✅ Estructura de hábitos completa:");
        System.out.println("   - Todos los campos necesarios presentes");
        System.out.println("   - Tipos de datos correctos");
    }

    @Test
    @DisplayName("CP05 - Estadísticas de filtrado por rol")
    void testEstadisticasFiltradoPorRol() throws Exception {
        System.out.println("=== Test: Estadísticas del sistema ===");

        long totalHabitos = habitoRepository.count();
        long habitosMID = habitoRepository.findAll().stream()
                .filter(h -> "MID".equals(h.getRol()))
                .count();

        long habitosTODOS = habitoRepository.findAll().stream()
                .filter(h -> "TODOS".equals(h.getRol()))
                .count();

        long habitosEspecificos = totalHabitos - habitosTODOS;

        System.out.println("📊 Estadísticas del sistema:");
        System.out.println("   - Total hábitos: " + totalHabitos);
        System.out.println("   - Hábitos MID: " + habitosMID);
        System.out.println("   - Hábitos TODOS: " + habitosTODOS);
        System.out.println("   - Hábitos rol específico: " + habitosEspecificos);
        System.out.println("   - Porcentaje MID: " + String.format("%.1f", (habitosMID * 100.0 / habitosEspecificos)) + "%");
        System.out.println("   - Porcentaje TODOS: " + String.format("%.1f", (habitosTODOS * 100.0 / totalHabitos)) + "%");

        assertTrue(totalHabitos > 0);
        assertTrue(habitosTODOS > 0);

        System.out.println("✅ Estadísticas calculadas correctamente");
    }
}
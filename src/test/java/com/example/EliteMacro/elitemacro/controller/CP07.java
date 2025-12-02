package com.example.EliteMacro.elitemacro.controller;

import com.example.EliteMacro.elitemacro.controller.BaseTest;
import com.example.EliteMacro.elitemacro.model.Habito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("CP07 - Historial de hábitos completados")
class CP07Test extends BaseTest {

    @BeforeEach
    void setUpHistorialHabitos() {
        // Crear hábitos completados para historial
        crearHabitoCompletado("Farmeo perfecto", "80+ CS a 10 min", "MID", usuarioTest, LocalDateTime.now().minusDays(1));
        crearHabitoCompletado("Control de visión", "3 wards en objetivos", "MID", usuarioTest, LocalDateTime.now().minusDays(2));
        crearHabitoCompletado("Roaming exitoso", "Gank exitoso en bot", "MID", usuarioTest, LocalDateTime.now().minusDays(3));

        // Hábito no completado
        Habito pendiente = new Habito();
        pendiente.setNombre("KDA positivo");
        pendiente.setDescripcion("KDA > 3.0");
        pendiente.setRol("MID");
        pendiente.setUsuario(usuarioTest);
        pendiente.setCompletado(false);
        habitoRepository.save(pendiente);

        // Hábitos del admin
        crearHabitoCompletado("Carry como ADC", "Daño más alto del equipo", "ADC", adminTest, LocalDateTime.now().minusDays(1));
    }

    private void crearHabitoCompletado(String nombre, String descripcion, String rol,
                                       com.example.EliteMacro.elitemacro.model.Usuario usuario,
                                       LocalDateTime fechaCompletado) {
        Habito habito = new Habito();
        habito.setNombre(nombre);
        habito.setDescripcion(descripcion);
        habito.setRol(rol);
        habito.setUsuario(usuario);
        habito.setCompletado(true);
        habito.setExperienciaOtorgada(true);
        habito.setFechaActualizacion(fechaCompletado);
        habitoRepository.save(habito);
    }

    @Test
    @WithMockUser(username = "midlaner123", roles = {"USER"})
    @DisplayName("CP07 - Ver historial de hábitos completados")
    void testCP07_VerHistorialCompletados() throws Exception {
        System.out.println("=== CP07: Historial de hábitos completados (HU-07) ===");
        System.out.println("Descripción: Ver historial de hábitos completados");
        System.out.println("Precondiciones: Al menos un hábito completado");
        System.out.println("Resultado esperado: Visualización del historial de hábitos");

        mockMvc.perform(get("/api/habitos/mis-habitos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4)) // 3 completados + 1 pendiente
                .andExpect(jsonPath("$[?(@.completado == true)].length()").value(3))
                .andExpect(jsonPath("$[?(@.nombre == 'Farmeo perfecto')].completado").value(true))
                .andExpect(jsonPath("$[?(@.nombre == 'Control de visión')].completado").value(true))
                .andExpect(jsonPath("$[?(@.nombre == 'Roaming exitoso')].completado").value(true))
                .andExpect(jsonPath("$[?(@.nombre == 'KDA positivo')].completado").value(false));

        // Verificar en base de datos
        List<Habito> habitosUsuario = habitoRepository.findByUsuario(usuarioTest);
        long completadosCount = habitosUsuario.stream()
                .filter(Habito::isCompletado)
                .count();

        assertEquals(3, completadosCount, "Debería tener 3 hábitos completados");

        System.out.println("✅ Caso de prueba CP07 EXITOSO");
        System.out.println("   - Hábitos completados encontrados: " + completadosCount);
        System.out.println("   - Hábitos totales: " + habitosUsuario.size());
        System.out.println("   - Porcentaje completado: " + (completadosCount * 100 / habitosUsuario.size()) + "%");

        // Mostrar historial
        System.out.println("   - Historial de hábitos completados:");
        habitosUsuario.stream()
                .filter(Habito::isCompletado)
                .forEach(h -> System.out.println("     • " + h.getNombre() +
                        " (" + h.getFechaActualizacion() + ")"));

        System.out.println("Estado: COMPLETADO ✓");
    }

    @Test
    @WithMockUser(username = "midlaner123", roles = {"USER"})
    @DisplayName("CP07 - Ver solo hábitos completados (filtrado)")
    void testFiltrarSoloCompletados() throws Exception {
        System.out.println("=== Test: Filtrar solo hábitos completados ===");

        mockMvc.perform(get("/api/habitos/mis-habitos"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    System.out.println("   - Respuesta completa: " + response);
                })
                .andExpect(jsonPath("$[?(@.completado == true)]").exists());

        System.out.println("✅ Puede filtrar hábitos completados");
    }

    @Test
    @WithMockUser(username = "adminchallenger", roles = {"ADMIN"})
    @DisplayName("CP07 - Admin ve su propio historial")
    void testAdminVeSuHistorial() throws Exception {
        System.out.println("=== Test: Admin ve su historial ===");

        mockMvc.perform(get("/api/habitos/mis-habitos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)) // 1 completado del admin
                .andExpect(jsonPath("$[0].nombre").value("Carry como ADC"))
                .andExpect(jsonPath("$[0].completado").value(true));

        System.out.println("✅ Admin ve solo sus hábitos completados");
    }

    @Test
    @WithMockUser(username = "midlaner123", roles = {"USER"})
    @DisplayName("CP07 - No ver hábitos completados de otros usuarios")
    void testNoVerHistorialDeOtros() throws Exception {
        System.out.println("=== Test: No ver historial ajeno ===");

        mockMvc.perform(get("/api/habitos/mis-habitos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre == 'Carry como ADC')]").doesNotExist());

        System.out.println("✅ Correctamente no muestra hábitos de otros usuarios");
    }

    @Test
    @DisplayName("CP07 - Calcular estadísticas de completado")
    void testEstadisticasCompletado() throws Exception {
        System.out.println("=== Test: Calcular estadísticas ===");

        List<Habito> todosHabitos = habitoRepository.findAll();
        long total = todosHabitos.size();
        long completados = todosHabitos.stream().filter(Habito::isCompletado).count();
        double porcentaje = (double) completados / total * 100;

        System.out.println("   - Total hábitos en sistema: " + total);
        System.out.println("   - Hábitos completados: " + completados);
        System.out.println("   - Porcentaje completado: " + String.format("%.1f", porcentaje) + "%");

        assertTrue(completados > 0, "Debería haber hábitos completados");
        assertTrue(porcentaje > 0, "Porcentaje debería ser positivo");

        System.out.println("✅ Estadísticas calculadas correctamente");
    }
}
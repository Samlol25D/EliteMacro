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

@DisplayName("CP03 - Crear hábito personalizado para mejora en League")
class CP03Test extends BaseTest {

    @BeforeEach
    void setUpHabitosIniciales() {
        // Crear algunos hábitos iniciales para pruebas
        crearHabitoBasico("Farmeo básico", "Practicar last hitting", "MID", usuarioTest);
    }

    private Habito crearHabitoBasico(String nombre, String descripcion, String rol, Usuario usuario) {
        Habito habito = new Habito();
        habito.setNombre(nombre);
        habito.setDescripcion(descripcion);
        habito.setRol(rol);
        habito.setUsuario(usuario);
        habito.setDificultad("MEDIA");
        habito.setFrecuencia("DIARIA");
        habito.setPuntosExperiencia(20);
        return habitoRepository.save(habito);
    }

    @Test
    @WithMockUser(username = "midlaner123", roles = {"USER"})
    @DisplayName("CP03 - Crear hábito personalizado para MID lane")
    void testCP03_CrearHabitoPersonalizadoMID() throws Exception {
        System.out.println("=== CP03: Crear hábito personalizado (HU-03) ===");
        System.out.println("Descripción: Invocador MID crea hábito para mejorar su juego");
        System.out.println("Precondiciones: Usuario autenticado como MID");
        System.out.println("Resultado esperado: Hábito almacenado correctamente");

        // Hábito específico para MID lane
        String habitoData = """
                {
                    "nombre": "Control de wave nivel 1-3",
                    "descripcion": "Manipular la wave para tener ventaja de nivel 2/3",
                    "rol": "MID",
                    "dificultad": "ALTA",
                    "frecuencia": "POR_PARTIDA"
                }
                """;

        mockMvc.perform(post("/api/habitos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(habitoData))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Control de wave nivel 1-3"))
                .andExpect(jsonPath("$.descripcion").value("Manipular la wave para tener ventaja de nivel 2/3"))
                .andExpect(jsonPath("$.rol").value("MID"))
                .andExpect(jsonPath("$.dificultad").value("ALTA"))
                .andExpect(jsonPath("$.frecuencia").value("POR_PARTIDA"))
                .andExpect(jsonPath("$.puntosExperiencia").value(30))
                .andExpect(jsonPath("$.completado").value(false))
                .andExpect(jsonPath("$.activo").value(true));

        // Verificar en base de datos
        Habito habitoGuardado = habitoRepository.findAll().stream()
                .filter(h -> h.getNombre().equals("Control de wave nivel 1-3"))
                .findFirst()
                .orElse(null);

        assertNotNull(habitoGuardado);
        assertEquals("midlaner123", habitoGuardado.getUsuario().getUsername());
        assertEquals("MID", habitoGuardado.getRol());
        assertEquals(30, habitoGuardado.getPuntosExperiencia());

        System.out.println("✅ Caso de prueba CP03 EXITOSO");
        System.out.println("   - Hábito creado: Control de wave nivel 1-3");
        System.out.println("   - Rol: MID");
        System.out.println("   - Dificultad: ALTA (30 XP)");
        System.out.println("   - Experiencia asignada: " + habitoGuardado.getPuntosExperiencia() + " XP");
        System.out.println("   - Usuario asignado: " + habitoGuardado.getUsuario().getUsername());
        System.out.println("Estado: COMPLETADO ✓");
    }

    @Test
    @WithMockUser(username = "midlaner123", roles = {"USER"})
    @DisplayName("CP03 - Crear hábito para Jungla con puntos específicos")
    void testCrearHabitoJunglaConPuntos() throws Exception {
        System.out.println("=== Test: Hábito de Jungla con experiencia específica ===");

        String habitoData = """
                {
                    "nombre": "Timer de objetivos épicos",
                    "descripcion": "Anotar timer de Dragón (5min) y Barón (7min)",
                    "rol": "JUNGLE",
                    "dificultad": "MEDIA",
                    "frecuencia": "POR_PARTIDA",
                    "puntosExperiencia": 25
                }
                """;

        mockMvc.perform(post("/api/habitos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(habitoData))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Timer de objetivos épicos"))
                .andExpect(jsonPath("$.puntosExperiencia").value(25));

        System.out.println("✅ Hábito de jungla creado con 25 XP específicos");
    }

    @Test
    @WithMockUser(username = "midlaner123", roles = {"USER"})
    @DisplayName("CP03 - Validar creación sin campos requeridos")
    void testValidarCamposRequeridos() throws Exception {
        System.out.println("=== Test: Validar campos requeridos ===");

        // Test 1: Sin nombre
        String habitoData = """
                {
                    "descripcion": "Sin nombre",
                    "rol": "MID"
                }
                """;

        mockMvc.perform(post("/api/habitos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(habitoData))
                .andExpect(status().isBadRequest());

        // Test 2: Sin descripción
        habitoData = """
                {
                    "nombre": "Sin descripción",
                    "rol": "MID"
                }
                """;

        mockMvc.perform(post("/api/habitos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(habitoData))
                .andExpect(status().isBadRequest());

        System.out.println("✅ Validación de campos requeridos funciona");
    }

    @Test
    @WithMockUser(username = "midlaner123", roles = {"USER"})
    @DisplayName("CP03 - Crear hábito con dificultad BAJA (10 XP)")
    void testCrearHabitoDificultadBaja() throws Exception {
        System.out.println("=== Test: Hábito con dificultad BAJA ===");

        String habitoData = """
                {
                    "nombre": "Usar trinket al inicio",
                    "descripcion": "Colocar trinket en river a los 1:30",
                    "rol": "TODOS",
                    "dificultad": "BAJA"
                }
                """;

        mockMvc.perform(post("/api/habitos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(habitoData))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntosExperiencia").value(10));

        System.out.println("✅ Hábito BAJA dificultad otorga 10 XP");
    }
}
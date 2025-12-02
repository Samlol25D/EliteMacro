package com.example.EliteMacro.elitemacro.controller;

import com.example.EliteMacro.elitemacro.controller.BaseTest;
import com.example.EliteMacro.elitemacro.model.Habito;
import com.example.EliteMacro.elitemacro.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("CP04 - Gestión administrador de hábitos predeterminados")
class CP04Test extends BaseTest {

    @BeforeEach
    void setUpDataAdmin() {
        // Crear usuarios adicionales para pruebas de admin
        crearUsuarioTest("toplanerMain", "top@elitemacro.com", "ROLE_USER");
        crearUsuarioTest("adcCarry", "adc@elitemacro.com", "ROLE_USER");
        crearUsuarioTest("supportMain", "support@elitemacro.com", "ROLE_USER");
    }

    @Test
    @WithMockUser(username = "adminchallenger", roles = {"ADMIN"})
    @DisplayName("CP04 - Admin crea hábito predeterminado para todos los usuarios")
    void testCP04_CrearHabitoPredeterminadoGlobal() throws Exception {
        System.out.println("=== CP04: Gestión administrador (HU-04) ===");
        System.out.println("Descripción: Admin crea hábitos predeterminados para todos");
        System.out.println("Precondiciones: Ingreso como administrador");
        System.out.println("Resultado esperado: Visualización y edición de hábitos predeterminados");

        // Contar usuarios activos antes de crear hábito
        List<Usuario> usuariosAntes = usuarioRepository.findAll().stream()
                .filter(Usuario::isActivo)
                .toList();
        int totalUsuariosActivos = usuariosAntes.size();

        System.out.println("   - Usuarios activos en sistema: " + totalUsuariosActivos);

        // 1. Admin crea hábito predeterminado para TODOS los roles
        String habitoData = """
            {
                "nombre": "Mirar minimapa cada 15 segundos",
                "descripcion": "Consciencia del mapa para evitar ganks y ver oportunidades",
                "rol": "TODOS",
                "dificultad": "MEDIA",
                "frecuencia": "DIARIA"
            }
            """;

        mockMvc.perform(post("/api/habitos/habitos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(habitoData))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Hábito creado para " + totalUsuariosActivos + " usuarios activos"));

        System.out.println("✅ 1. Hábito predeterminado creado exitosamente");

        // 2. Verificar que se creó para cada usuario activo
        for (Usuario usuario : usuariosAntes) {
            List<Habito> habitosUsuario = habitoRepository.findByUsuario(usuario);
            boolean tieneHabitoPredeterminado = habitosUsuario.stream()
                    .anyMatch(h -> h.getNombre().equals("Mirar minimapa cada 15 segundos"));

            assertTrue(tieneHabitoPredeterminado,
                    "Usuario " + usuario.getUsername() + " debería tener el hábito predeterminado");

            System.out.println("   - Usuario " + usuario.getUsername() + ": Hábito asignado ✓");
        }

        // 3. Admin puede ver todos los hábitos creados
        mockMvc.perform(get("/api/habitos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(totalUsuariosActivos))
                .andExpect(jsonPath("$[?(@.nombre == 'Mirar minimapa cada 15 segundos')]").exists());

        System.out.println("✅ 2. Admin puede visualizar todos los hábitos");

        // 4. Ver detalles de un hábito específico
        Habito primerHabito = habitoRepository.findAll().get(0);
        mockMvc.perform(get("/api/habitos/{id}", primerHabito.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Mirar minimapa cada 15 segundos"))
                .andExpect(jsonPath("$.descripcion").value("Consciencia del mapa para evitar ganks y ver oportunidades"))
                .andExpect(jsonPath("$.rol").value("TODOS"))
                .andExpect(jsonPath("$.dificultad").value("MEDIA"))
                .andExpect(jsonPath("$.puntosExperiencia").value(20));

        System.out.println("✅ 3. Detalles de hábito accesibles");

        // 5. Admin puede editar hábito predeterminado
        String updateData = """
            {
                "nombre": "Mirar minimapa cada 10 segundos (Mejorado)",
                "descripcion": "Consciencia del mapa mejorada - cada 10 segundos",
                "dificultad": "ALTA"
            }
            """;

        mockMvc.perform(put("/api/habitos/{id}", primerHabito.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateData))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Mirar minimapa cada 10 segundos (Mejorado)"))
                .andExpect(jsonPath("$.dificultad").value("ALTA"))
                .andExpect(jsonPath("$.puntosExperiencia").value(30));

        System.out.println("✅ 4. Edición de hábito exitosa");

        // 6. Admin puede eliminar hábito
        mockMvc.perform(delete("/api/habitos/{id}", primerHabito.getId()))
                .andExpect(status().isOk());

        // Verificar que se eliminó
        assertFalse(habitoRepository.findById(primerHabito.getId()).isPresent());

        System.out.println("✅ 5. Eliminación de hábito exitosa");

        System.out.println("\n✅ Caso de prueba CP04 EXITOSO");
        System.out.println("   - Hábito predeterminado creado: Mirar minimapa");
        System.out.println("   - Usuarios afectados: " + totalUsuariosActivos);
        System.out.println("   - Rol asignado: TODOS");
        System.out.println("   - Experiencia por hábito: 20 XP (MEDIA) → 30 XP (ALTA)");
        System.out.println("   - Operaciones completadas: Crear, Leer, Actualizar, Eliminar");
        System.out.println("Estado: COMPLETADO ✓");
    }

    @Test
    @WithMockUser(username = "adminchallenger", roles = {"ADMIN"})
    @DisplayName("CP04 - Admin crea hábito específico por rol")
    void testCrearHabitoPorRolEspecifico() throws Exception {
        System.out.println("=== Test: Admin crea hábito para rol específico ===");

        // Contar usuarios con rol específico
        long usuariosMID = usuarioRepository.findAll().stream()
                .filter(u -> u.isActivo())
                .count(); // Todos activos en esta prueba

        // Hábito específico para MID lane
        String habitoData = """
            {
                "nombre": "Control de wave nivel 1-3 (MID)",
                "descripcion": "Manipulación de wave para ventaja de nivel temprano",
                "rol": "MID",
                "dificultad": "ALTA",
                "frecuencia": "POR_PARTIDA"
            }
            """;

        mockMvc.perform(post("/api/habitos/habitos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(habitoData))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Hábito creado para " + usuariosMID + " usuarios activos"));

        // Verificar que se creó con atributos correctos
        List<Habito> habitosCreados = habitoRepository.findAll();
        assertFalse(habitosCreados.isEmpty());

        Habito habitoCreado = habitosCreados.get(0);
        assertEquals("Control de wave nivel 1-3 (MID)", habitoCreado.getNombre());
        assertEquals("MID", habitoCreado.getRol());
        assertEquals(30, habitoCreado.getPuntosExperiencia());

        System.out.println("✅ Hábito específico por rol creado:");
        System.out.println("   - Nombre: " + habitoCreado.getNombre());
        System.out.println("   - Rol: " + habitoCreado.getRol());
        System.out.println("   - Dificultad: " + habitoCreado.getDificultad());
        System.out.println("   - Experiencia: " + habitoCreado.getPuntosExperiencia() + " XP");
    }

    @Test
    @WithMockUser(username = "midlaner123", roles = {"USER"})
    @DisplayName("CP04 - Usuario normal NO puede crear hábitos predeterminados")
    void testUsuarioNormalNoPuedeCrearPredeterminados() throws Exception {
        System.out.println("=== Test: Validar permisos de usuario normal ===");

        String habitoData = """
            {
                "nombre": "Hábito no autorizado",
                "descripcion": "Usuario normal intentando crear hábito predeterminado",
                "rol": "TODOS"
            }
            """;

        mockMvc.perform(post("/api/habitos/habitos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(habitoData))
                .andExpect(status().isForbidden());

        System.out.println("✅ Correctamente denegado acceso a usuario normal");
    }

    @Test
    @WithMockUser(username = "adminchallenger", roles = {"ADMIN"})
    @DisplayName("CP04 - Admin puede gestionar hábitos de otros usuarios")
    void testAdminGestionaHabitosDeOtros() throws Exception {
        System.out.println("=== Test: Admin gestiona hábitos ajenos ===");

        // Crear hábito para usuario normal
        Habito habitoUsuario = new Habito();
        habitoUsuario.setNombre("Farmeo perfecto");
        habitoUsuario.setDescripcion("80+ CS a 10 minutos");
        habitoUsuario.setUsuario(usuarioTest);
        habitoUsuario.setRol("MID");
        habitoRepository.save(habitoUsuario);

        Long habitoId = habitoUsuario.getId();

        // 1. Admin puede ver hábito de otro usuario
        mockMvc.perform(get("/api/habitos/{id}", habitoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Farmeo perfecto"));

        System.out.println("✅ 1. Puede ver hábitos de otros usuarios");

        // 2. Admin puede editar hábito de otro usuario
        String updateData = """
            {
                "nombre": "Farmeo perfecto (Editado por Admin)",
                "descripcion": "85+ CS a 10 minutos - estándar más alto",
                "dificultad": "ALTA"
            }
            """;

        mockMvc.perform(put("/api/habitos/{id}", habitoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateData))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Farmeo perfecto (Editado por Admin)"))
                .andExpect(jsonPath("$.dificultad").value("ALTA"))
                .andExpect(jsonPath("$.puntosExperiencia").value(30));

        System.out.println("✅ 2. Puede editar hábitos de otros usuarios");

        // 3. Admin puede eliminar hábito de otro usuario
        mockMvc.perform(delete("/api/habitos/{id}", habitoId))
                .andExpect(status().isOk());

        assertFalse(habitoRepository.findById(habitoId).isPresent());

        System.out.println("✅ 3. Puede eliminar hábitos de otros usuarios");
    }

    @Test
    @WithMockUser(username = "adminchallenger", roles = {"ADMIN"})
    @DisplayName("CP04 - Validaciones al crear hábitos predeterminados")
    void testValidacionesCreacionHabitos() throws Exception {
        System.out.println("=== Test: Validaciones de creación ===");

        // Test 1: Sin nombre
        String habitoData = """
            {
                "descripcion": "Sin nombre",
                "rol": "TODOS"
            }
            """;

        mockMvc.perform(post("/api/habitos/habitos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(habitoData))
                .andExpect(status().isBadRequest());

        System.out.println("✅ 1. Validación: Nombre requerido");

        // Test 2: Sin descripción
        habitoData = """
            {
                "nombre": "Sin descripción",
                "rol": "TODOS"
            }
            """;

        mockMvc.perform(post("/api/habitos/habitos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(habitoData))
                .andExpect(status().isBadRequest());

        System.out.println("✅ 2. Validación: Descripción requerida");

        // Test 3: Con datos válidos
        habitoData = """
            {
                "nombre": "Hábito válido",
                "descripcion": "Descripción válida",
                "rol": "TODOS"
            }
            """;

        mockMvc.perform(post("/api/habitos/habitos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(habitoData))
                .andExpect(status().isOk());

        System.out.println("✅ 3. Creación exitosa con datos válidos");
    }

    @Test
    @WithMockUser(username = "adminchallenger", roles = {"ADMIN"})
    @DisplayName("CP04 - Admin ve dashboard completo del sistema")
    void testAdminDashboardCompleto() throws Exception {
        System.out.println("=== Test: Dashboard de administrador ===");

        // Crear datos de prueba
        for (int i = 1; i <= 5; i++) {
            Usuario usuario = crearUsuarioTest("user" + i, "user" + i + "@elitemacro.com", "ROLE_USER");

            // Crear hábitos para cada usuario
            for (int j = 1; j <= 3; j++) {
                Habito habito = new Habito();
                habito.setNombre("Hábito " + j + " de user" + i);
                habito.setDescripcion("Descripción del hábito");
                habito.setUsuario(usuario);
                habito.setRol("TODOS");
                habito.setCompletado(j % 2 == 0); // Alternar completado
                habitoRepository.save(habito);
            }
        }

        // Admin puede ver todos los hábitos del sistema
        mockMvc.perform(get("/api/habitos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(15)); // 5 usuarios * 3 hábitos

        System.out.println("✅ Dashboard admin muestra todos los hábitos:");
        System.out.println("   - Total hábitos en sistema: 15");
        System.out.println("   - Usuarios en sistema: " + usuarioRepository.count());
        System.out.println("   - Hábitos por usuario: 3");

        // Contar hábitos completados
        long completados = habitoRepository.findAll().stream()
                .filter(Habito::isCompletado)
                .count();

        System.out.println("   - Hábitos completados: " + completados);
        System.out.println("   - Porcentaje completado: " + (completados * 100 / 15) + "%");
    }
}
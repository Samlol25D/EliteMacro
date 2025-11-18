package com.example.EliteMacro.elitemacro.controller;

import com.example.EliteMacro.elitemacro.model.Habito;
import com.example.EliteMacro.elitemacro.model.Usuario;
import com.example.EliteMacro.elitemacro.service.UsuarioService;
import com.example.EliteMacro.elitemacro.repository.HabitoRepository;
import com.example.EliteMacro.elitemacro.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/habitos")
@CrossOrigin(origins = "*")
public class HabitoController {

    @Autowired
    private HabitoRepository habitoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<?> listarHabitos() {
        try {
            System.out.println("=== LISTANDO TODOS LOS HÁBITOS ===");
            List<Habito> habitos = habitoRepository.findAll();
            System.out.println("Total de hábitos encontrados: " + habitos.size());

            for (Habito habito : habitos) {
                System.out.println(" - Hábito ID: " + habito.getId() +
                        ", Nombre: " + habito.getNombre() +
                        ", Usuario: " + (habito.getUsuario() != null ? habito.getUsuario().getUsername() : "NULL"));
            }

            return ResponseEntity.ok(habitos);
        } catch (Exception e) {
            System.out.println("ERROR al listar hábitos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al listar hábitos: " + e.getMessage());
        }
    }

    @PostMapping("/habitos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearHabitoPredeterminado(@RequestBody Habito habito) {
        try {
            System.out.println("=== CREANDO HÁBITO PREDETERMINADO ===");

            // Validaciones
            if (habito.getNombre() == null || habito.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre del hábito es requerido");
            }
            if (habito.getDescripcion() == null || habito.getDescripcion().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La descripción del hábito es requerida");
            }

            // Obtener todos los usuarios activos
            List<Usuario> usuariosActivos = usuarioRepository.findAll().stream()
                    .filter(Usuario::isActivo)
                    .toList();

            System.out.println("Usuarios activos encontrados: " + usuariosActivos.size());

            // Crear el hábito para cada usuario activo
            for (Usuario usuario : usuariosActivos) {
                Habito nuevoHabito = new Habito();
                nuevoHabito.setNombre(habito.getNombre().trim());
                nuevoHabito.setDescripcion(habito.getDescripcion().trim());
                nuevoHabito.setRol(habito.getRol() != null ? habito.getRol() : "TODOS");
                nuevoHabito.setCompletado(false);
                nuevoHabito.setUsuario(usuario); // Asignar a usuario específico

                habitoRepository.save(nuevoHabito);
                System.out.println("Hábito creado para usuario: " + usuario.getUsername());
            }

            System.out.println("=== HÁBITOS CREADOS EXITOSAMENTE ===");
            System.out.println("Total de hábitos creados: " + usuariosActivos.size());

            return ResponseEntity.ok("Hábito creado para " + usuariosActivos.size() + " usuarios activos");

        } catch (Exception e) {
            System.out.println("ERROR al crear hábitos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al crear hábitos: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerHabito(@PathVariable Long id) {
        System.out.println("=== OBTENIENDO HÁBITO POR ID ===");
        System.out.println("ID solicitado: " + id);

        Optional<Habito> habitoOpt = habitoRepository.findById(id);
        if (habitoOpt.isPresent()) {
            Habito habito = habitoOpt.get();
            System.out.println("Hábito encontrado: " + habito.getNombre());

            // Crear respuesta simplificada
            Map<String, Object> response = new HashMap<>();
            response.put("id", habito.getId());
            response.put("nombre", habito.getNombre());
            response.put("descripcion", habito.getDescripcion());
            response.put("rol", habito.getRol());
            response.put("dificultad", habito.getDificultad());
            response.put("frecuencia", habito.getFrecuencia());
            response.put("puntosExperiencia", habito.getPuntosExperiencia());
            response.put("completado", habito.isCompletado());
            response.put("activo", habito.isActivo());
            response.put("fechaCreacion", habito.getFechaCreacion());
            response.put("fechaActualizacion", habito.getFechaActualizacion());

            return ResponseEntity.ok(response);
        } else {
            System.out.println("Hábito no encontrado con ID: " + id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/mis-habitos")
    public ResponseEntity<?> listarMisHabitos(Authentication auth) {
        try {
            System.out.println("=== LISTANDO MIS HÁBITOS ===");

            if (auth == null) {
                System.out.println("ERROR: No hay autenticación");
                return ResponseEntity.status(401).body("No estás autenticado");
            }

            String username = auth.getName();
            System.out.println("Usuario autenticado: " + username);

            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
            if (usuarioOpt.isEmpty()) {
                System.out.println("ERROR: Usuario no encontrado: " + username);
                return ResponseEntity.badRequest().body("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();
            System.out.println("Buscando hábitos del usuario: " + usuario.getUsername());

            List<Habito> habitos = habitoRepository.findByUsuario(usuario);
            System.out.println("Hábitos encontrados: " + habitos.size());

            for (Habito habito : habitos) {
                System.out.println(" - " + habito.getNombre() + " (ID: " + habito.getId() + ")");
            }

            return ResponseEntity.ok(habitos);

        } catch (Exception e) {
            System.out.println("ERROR al listar mis hábitos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al obtener hábitos: " + e.getMessage());
        }
    }
    @PostMapping
    public ResponseEntity<?> crearHabito(@RequestBody Habito habito, Authentication authentication) {
        try {
            System.out.println("=== CREANDO NUEVO HÁBITO PERSONAL ===");
            System.out.println("Hábito recibido: " + habito);

            if (authentication == null) {
                return ResponseEntity.status(401).body("No estás autenticado");
            }

            String username = authentication.getName();
            System.out.println("Usuario autenticado: " + username);

            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
            if (usuarioOpt.isEmpty()) {
                System.out.println("ERROR: Usuario no encontrado: " + username);
                return ResponseEntity.badRequest().body("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();

            // Validaciones básicas
            if (habito.getNombre() == null || habito.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre del hábito es requerido");
            }
            if (habito.getDescripcion() == null || habito.getDescripcion().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La descripción del hábito es requerida");
            }

            // Configurar el hábito
            habito.setUsuario(usuario);
            habito.setActivo(true);
            habito.setCompletado(false);
            habito.setExperienciaOtorgada(false);

            // Establecer valores por defecto si no vienen
            if (habito.getDificultad() == null) {
                habito.setDificultad("MEDIA");
            }
            if (habito.getFrecuencia() == null) {
                habito.setFrecuencia("DIARIA");
            }
            if (habito.getPuntosExperiencia() == 0) {
                // Asignar puntos según dificultad
                switch (habito.getDificultad().toUpperCase()) {
                    case "BAJA": habito.setPuntosExperiencia(10); break;
                    case "MEDIA": habito.setPuntosExperiencia(20); break;
                    case "ALTA": habito.setPuntosExperiencia(30); break;
                    default: habito.setPuntosExperiencia(20);
                }
            }

            System.out.println("Hábito a guardar: " + habito);

            Habito nuevoHabito = habitoRepository.save(habito);
            System.out.println("Hábito guardado exitosamente: " + nuevoHabito.getId());

            return ResponseEntity.ok(nuevoHabito);

        } catch (Exception e) {
            System.err.println("Error al crear hábito: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al crear el hábito: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> actualizarHabito(
            @PathVariable Long id,
            @RequestBody Habito nuevo,
            Authentication auth) {

        System.out.println("=== ACTUALIZANDO HÁBITO ===");
        System.out.println("ID del hábito: " + id);

        if (auth == null) {
            return ResponseEntity.status(401).body("No estás autenticado");
        }

        Optional<Habito> habitoOpt = habitoRepository.findById(id);
        if (habitoOpt.isEmpty()) {
            System.out.println("Hábito no encontrado con ID: " + id);
            return ResponseEntity.notFound().build();
        }

        Habito habitoExistente = habitoOpt.get();
        String username = auth.getName();

        try {
            // Verificar permisos
            boolean esDuenio = habitoExistente.getUsuario().getUsername().equals(username);
            boolean esAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!esDuenio && !esAdmin) {
                return ResponseEntity.status(403).body("No tienes permisos para editar este hábito");
            }

            boolean estabaCompletado = habitoExistente.isCompletado();
            boolean ahoraCompletado = nuevo.isCompletado();
            boolean experienciaYaOtorgada = habitoExistente.isExperienciaOtorgada();

            System.out.println("Estado anterior: " + (estabaCompletado ? "COMPLETADO" : "PENDIENTE"));
            System.out.println("Estado nuevo: " + (ahoraCompletado ? "COMPLETADO" : "PENDIENTE"));
            System.out.println("Experiencia ya otorgada: " + experienciaYaOtorgada);

            // LÓGICA MEJORADA PARA EXPERIENCIA
            boolean seCompletoPorPrimeraVez = !estabaCompletado && ahoraCompletado && !experienciaYaOtorgada;
            boolean seRecompleta = !estabaCompletado && ahoraCompletado && experienciaYaOtorgada;
            boolean seDesmarca = estabaCompletado && !ahoraCompletado;

            // Actualizar campos básicos
            if (nuevo.getNombre() != null) habitoExistente.setNombre(nuevo.getNombre());
            if (nuevo.getDescripcion() != null) habitoExistente.setDescripcion(nuevo.getDescripcion());
            if (nuevo.getRol() != null) habitoExistente.setRol(nuevo.getRol());
            if (nuevo.getDificultad() != null) habitoExistente.setDificultad(nuevo.getDificultad());
            if (nuevo.getFrecuencia() != null) habitoExistente.setFrecuencia(nuevo.getFrecuencia());

            // Actualizar estado de completado
            habitoExistente.setCompletado(ahoraCompletado);

            // Manejar experiencia
            if (seCompletoPorPrimeraVez) {
                // Primera vez que se completa → sumar experiencia
                int experiencia = habitoExistente.getPuntosExperiencia();
                usuarioService.agregarExperiencia(username, experiencia);
                habitoExistente.setExperienciaOtorgada(true);
                System.out.println("✅ Experiencia agregada por primera vez: " + experiencia + " XP para " + username);

            } else if (seRecompleta) {
                // Se está completando de nuevo después de haber sido desmarcado
                // PERO ya se otorgó experiencia antes → NO sumar de nuevo
                System.out.println("⚠️ Hábito recompletado, pero no se otorga experiencia adicional (ya fue otorgada)");

            } else if (seDesmarca) {
                // Se está desmarcando un hábito completado
                // La experiencia otorgada se mantiene en true, pero no se resta experiencia
                System.out.println("ℹ️ Hábito desmarcado, experiencia otorgada se mantiene: " + experienciaYaOtorgada);

            } else if (estabaCompletado && ahoraCompletado) {
                System.out.println("ℹ️ Hábito ya estaba completado, no se realiza acción");
            } else {
                System.out.println("ℹ️ Hábito sigue pendiente, no se agrega experiencia");
            }

            Habito actualizado = habitoRepository.save(habitoExistente);
            System.out.println("Hábito actualizado exitosamente");

            // Crear respuesta simplificada
            Map<String, Object> response = new HashMap<>();
            response.put("id", actualizado.getId());
            response.put("nombre", actualizado.getNombre());
            response.put("descripcion", actualizado.getDescripcion());
            response.put("rol", actualizado.getRol());
            response.put("dificultad", actualizado.getDificultad());
            response.put("frecuencia", actualizado.getFrecuencia());
            response.put("puntosExperiencia", actualizado.getPuntosExperiencia());
            response.put("completado", actualizado.isCompletado());
            response.put("experienciaOtorgada", actualizado.isExperienciaOtorgada());
            response.put("activo", actualizado.isActivo());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("ERROR al actualizar hábito: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al actualizar el hábito: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarHabito(
            @PathVariable Long id,
            Authentication auth) {

        System.out.println("=== ELIMINANDO HÁBITO ===");
        System.out.println("ID del hábito: " + id);

        if (auth == null) {
            return ResponseEntity.status(401).body("No estás autenticado");
        }

        Optional<Habito> habitoOpt = habitoRepository.findById(id);
        if (habitoOpt.isEmpty()) {
            System.out.println("Hábito no encontrado con ID: " + id);
            return ResponseEntity.notFound().build();
        }

        Habito h = habitoOpt.get();
        String username = auth.getName();

        // Verificar permisos: dueño o administrador
        boolean esDuenio = h.getUsuario().getUsername().equals(username);
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        System.out.println("Es dueño: " + esDuenio);
        System.out.println("Es admin: " + esAdmin);

        if (!esDuenio && !esAdmin) {
            System.out.println("ERROR: Sin permisos para eliminar");
            return ResponseEntity.status(403).body("No tienes permisos para eliminar este hábito");
        }

        System.out.println("Eliminando hábito: " + h.getNombre());
        habitoRepository.deleteById(id);
        System.out.println("Hábito eliminado exitosamente");

        return ResponseEntity.ok().body("Hábito eliminado correctamente");
    }
}

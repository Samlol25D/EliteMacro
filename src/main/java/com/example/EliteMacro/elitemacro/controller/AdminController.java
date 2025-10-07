package com.example.EliteMacro.elitemacro.controller;

import com.example.EliteMacro.elitemacro.model.Habito;
import com.example.EliteMacro.elitemacro.model.Usuario;
import com.example.EliteMacro.elitemacro.repository.HabitoRepository;
import com.example.EliteMacro.elitemacro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HabitoRepository habitoRepository;

    // ===== ENDPOINTS PARA USUARIOS =====

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Usuario>> obtenerUsuarios() {
        try {
            System.out.println("=== OBTENIENDO TODOS LOS USUARIOS ===");
            List<Usuario> usuarios = usuarioRepository.findAll();
            System.out.println("Usuarios encontrados: " + usuarios.size());

            // Log para debug
            for (Usuario usuario : usuarios) {
                System.out.println(" - Usuario: " + usuario.getUsername() +
                        ", ID: " + usuario.getId() +
                        ", Activo: " + usuario.isActivo() +
                        ", Rol: " + (usuario.getAuthorities() != null ? usuario.getAuthorities() : "ROLE_USER"));
            }

            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            System.out.println("ERROR al obtener usuarios: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
        try {
            System.out.println("=== OBTENIENDO USUARIO POR ID ===");
            System.out.println("ID solicitado: " + id);

            Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                System.out.println("Usuario encontrado: " + usuario.getUsername());
                return ResponseEntity.ok(usuario);
            } else {
                System.out.println("Usuario no encontrado con ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("ERROR al obtener usuario: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/usuarios/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cambiarEstadoUsuario(@PathVariable Long id, @RequestParam boolean activo) {
        try {
            System.out.println("=== CAMBIANDO ESTADO DE USUARIO ===");
            System.out.println("ID usuario: " + id + ", Nuevo estado: " + activo);

            Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                System.out.println("Usuario antes del cambio: " + usuario.getUsername() + ", Activo: " + usuario.isActivo());

                usuario.setActivo(activo);
                Usuario usuarioActualizado = usuarioRepository.save(usuario);

                System.out.println("Usuario después del cambio: " + usuarioActualizado.getUsername() + ", Activo: " + usuarioActualizado.isActivo());
                return ResponseEntity.ok("Estado actualizado a " + (activo ? "activo" : "inactivo"));
            } else {
                System.out.println("Usuario no encontrado con ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("ERROR al cambiar estado de usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al cambiar estado: " + e.getMessage());
        }
    }

    // ===== ENDPOINTS PARA HÁBITOS PREDETERMINADOS =====

    @PostMapping("/habitos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearHabitoPredeterminado(@RequestBody Habito habito) {
        try {
            System.out.println("=== CREANDO HÁBITO PREDETERMINADO ===");
            System.out.println("Datos recibidos:");
            System.out.println(" - Nombre: " + habito.getNombre());
            System.out.println(" - Descripción: " + habito.getDescripcion());
            System.out.println(" - Rol: " + habito.getRol());
            System.out.println(" - Completado: " + habito.isCompletado());

            // Validar datos requeridos
            if (habito.getNombre() == null || habito.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre del hábito es requerido");
            }

            if (habito.getDescripcion() == null || habito.getDescripcion().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La descripción del hábito es requerida");
            }

            // Asegurar que el rol tenga un valor por defecto si es null
            if (habito.getRol() == null) {
                habito.setRol("TODOS");
            }

            // Crear nuevo hábito predeterminado (sin usuario asignado)
            Habito nuevoHabito = new Habito();
            nuevoHabito.setNombre(habito.getNombre().trim());
            nuevoHabito.setDescripcion(habito.getDescripcion().trim());
            nuevoHabito.setRol(habito.getRol());
            nuevoHabito.setCompletado(false);
            nuevoHabito.setUsuario(null); // Hábito predeterminado, sin usuario

            System.out.println("Guardando hábito en la base de datos...");
            Habito habitoGuardado = habitoRepository.save(nuevoHabito);

            System.out.println("=== HÁBITO CREADO EXITOSAMENTE ===");
            System.out.println("ID del hábito: " + habitoGuardado.getId());
            System.out.println("Nombre: " + habitoGuardado.getNombre());
            System.out.println("Rol: " + habitoGuardado.getRol());

            return ResponseEntity.ok(habitoGuardado);

        } catch (Exception e) {
            System.out.println("=== ERROR AL CREAR HÁBITO ===");
            System.out.println("Mensaje de error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al crear hábito: " + e.getMessage());
        }
    }

    @GetMapping("/habitos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarHabitosAdmin() {
        try {
            System.out.println("=== LISTANDO TODOS LOS HÁBITOS (ADMIN) ===");
            List<Habito> habitos = habitoRepository.findAll();
            System.out.println("Total de hábitos encontrados: " + habitos.size());

            // Log para debug
            for (Habito habito : habitos) {
                System.out.println(" - Hábito ID: " + habito.getId() +
                        ", Nombre: " + habito.getNombre() +
                        ", Rol: " + habito.getRol() +
                        ", Usuario: " + (habito.getUsuario() != null ? habito.getUsuario().getUsername() : "PREDETERMINADO"));
            }

            return ResponseEntity.ok(habitos);
        } catch (Exception e) {
            System.out.println("ERROR al listar hábitos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al listar hábitos: " + e.getMessage());
        }
    }

    @PutMapping("/habitos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> editarHabito(@PathVariable Long id, @RequestBody Habito habitoActualizado) {
        try {
            System.out.println("=== EDITANDO HÁBITO ===");
            System.out.println("ID del hábito: " + id);
            System.out.println("Datos nuevos:");
            System.out.println(" - Nombre: " + habitoActualizado.getNombre());
            System.out.println(" - Descripción: " + habitoActualizado.getDescripcion());
            System.out.println(" - Rol: " + habitoActualizado.getRol());

            Optional<Habito> habitoOpt = habitoRepository.findById(id);
            if (habitoOpt.isPresent()) {
                Habito habitoExistente = habitoOpt.get();

                System.out.println("Hábito antes de editar:");
                System.out.println(" - Nombre: " + habitoExistente.getNombre());
                System.out.println(" - Descripción: " + habitoExistente.getDescripcion());
                System.out.println(" - Rol: " + habitoExistente.getRol());

                // Actualizar solo los campos permitidos
                habitoExistente.setNombre(habitoActualizado.getNombre());
                habitoExistente.setDescripcion(habitoActualizado.getDescripcion());
                habitoExistente.setRol(habitoActualizado.getRol());

                Habito habitoGuardado = habitoRepository.save(habitoExistente);

                System.out.println("=== HÁBITO ACTUALIZADO EXITOSAMENTE ===");
                System.out.println("Nuevos datos:");
                System.out.println(" - Nombre: " + habitoGuardado.getNombre());
                System.out.println(" - Descripción: " + habitoGuardado.getDescripcion());
                System.out.println(" - Rol: " + habitoGuardado.getRol());

                return ResponseEntity.ok(habitoGuardado);
            } else {
                System.out.println("Hábito no encontrado con ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("ERROR al editar hábito: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al editar hábito: " + e.getMessage());
        }
    }

    @DeleteMapping("/habitos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarHabito(@PathVariable Long id) {
        try {
            System.out.println("=== ELIMINANDO HÁBITO ===");
            System.out.println("ID del hábito: " + id);

            Optional<Habito> habitoOpt = habitoRepository.findById(id);
            if (habitoOpt.isPresent()) {
                Habito habito = habitoOpt.get();
                System.out.println("Eliminando hábito: " + habito.getNombre() + " (ID: " + habito.getId() + ")");

                habitoRepository.deleteById(id);

                System.out.println("=== HÁBITO ELIMINADO EXITOSAMENTE ===");
                return ResponseEntity.ok("Hábito eliminado correctamente");
            } else {
                System.out.println("Hábito no encontrado con ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("ERROR al eliminar hábito: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al eliminar hábito: " + e.getMessage());
        }
    }

    // ===== ENDPOINT PARA GENERAR REPORTES PDF =====

    @GetMapping("/usuarios/{id}/reporte-pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generarReportePDF(@PathVariable Long id) {
        try {
            System.out.println("=== GENERANDO REPORTE PDF ===");
            System.out.println("ID usuario: " + id);

            Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();

                // Por ahora solo devolvemos un mensaje
                // Aquí puedes integrar una librería como iText o Apache PDFBox para generar PDFs
                String mensaje = "Reporte PDF generado para usuario: " + usuario.getUsername() +
                        "\nEsta funcionalidad está en desarrollo.";

                System.out.println(mensaje);
                return ResponseEntity.ok(mensaje);
            } else {
                System.out.println("Usuario no encontrado para generar reporte: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("ERROR al generar reporte PDF: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error al generar reporte: " + e.getMessage());
        }
    }
}
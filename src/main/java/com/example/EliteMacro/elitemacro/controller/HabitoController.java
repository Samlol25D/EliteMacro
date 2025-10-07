package com.example.EliteMacro.elitemacro.controller;

import com.example.EliteMacro.elitemacro.model.Habito;
import com.example.EliteMacro.elitemacro.model.Usuario;
import com.example.EliteMacro.elitemacro.repository.HabitoRepository;
import com.example.EliteMacro.elitemacro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/habitos")
@CrossOrigin(origins = "*")
public class HabitoController {

    @Autowired
    private HabitoRepository habitoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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

    @PostMapping
    public ResponseEntity<?> crearHabito(@RequestBody Habito habito, Authentication auth) {
        try {
            System.out.println("=== INICIANDO CREACIÓN DE HÁBITO ===");

            if (auth == null) {
                System.out.println("ERROR: No hay autenticación");
                return ResponseEntity.status(401).body("No estás autenticado");
            }

            String username = auth.getName();
            System.out.println("Usuario autenticado: " + username);

            // Obtener el usuario autenticado
            Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
            if (usuarioOpt.isEmpty()) {
                System.out.println("ERROR: Usuario no encontrado en BD: " + username);
                return ResponseEntity.badRequest().body("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();
            System.out.println("Usuario encontrado - ID: " + usuario.getId() + ", Username: " + usuario.getUsername());

            // Mostrar datos del hábito recibido
            System.out.println("Datos del hábito recibidos:");
            System.out.println(" - Nombre: " + habito.getNombre());
            System.out.println(" - Descripción: " + habito.getDescripcion());
            System.out.println(" - Rol: " + habito.getRol());

            // Crear nuevo hábito con el usuario asignado
            Habito nuevoHabito = new Habito();
            nuevoHabito.setNombre(habito.getNombre());
            nuevoHabito.setDescripcion(habito.getDescripcion());
            nuevoHabito.setRol(habito.getRol());
            nuevoHabito.setCompletado(false);
            nuevoHabito.setUsuario(usuario);

            System.out.println("Guardando hábito en la base de datos...");
            Habito habitoGuardado = habitoRepository.save(nuevoHabito);

            System.out.println("=== HÁBITO GUARDADO EXITOSAMENTE ===");
            System.out.println("ID del hábito guardado: " + habitoGuardado.getId());
            System.out.println("Nombre: " + habitoGuardado.getNombre());
            System.out.println("Usuario asignado: " + habitoGuardado.getUsuario().getUsername());

            return ResponseEntity.ok(habitoGuardado);

        } catch (Exception e) {
            System.out.println("=== ERROR AL CREAR HÁBITO ===");
            System.out.println("Mensaje de error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al crear hábito: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habito> obtenerHabito(@PathVariable Long id) {
        System.out.println("=== OBTENIENDO HÁBITO POR ID ===");
        System.out.println("ID solicitado: " + id);

        Optional<Habito> habitoOpt = habitoRepository.findById(id);
        if (habitoOpt.isPresent()) {
            Habito habito = habitoOpt.get();
            System.out.println("Hábito encontrado: " + habito.getNombre());
            return ResponseEntity.ok(habito);
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

    @PutMapping("/{id}")
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

        Habito h = habitoOpt.get();
        String username = auth.getName();

        // Verificar permisos: dueño o administrador
        boolean esDuenio = h.getUsuario().getUsername().equals(username);
        boolean esAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        System.out.println("Es dueño: " + esDuenio);
        System.out.println("Es admin: " + esAdmin);

        if (!esDuenio && !esAdmin) {
            System.out.println("ERROR: Sin permisos para editar");
            return ResponseEntity.status(403).body("No tienes permisos para editar este hábito");
        }

        // Actualizar campos permitidos
        System.out.println("Actualizando hábito:");
        System.out.println(" - Nombre: " + h.getNombre() + " -> " + nuevo.getNombre());
        System.out.println(" - Descripción: " + h.getDescripcion() + " -> " + nuevo.getDescripcion());
        System.out.println(" - Rol: " + h.getRol() + " -> " + nuevo.getRol());
        System.out.println(" - Completado: " + h.isCompletado() + " -> " + nuevo.isCompletado());

        h.setNombre(nuevo.getNombre());
        h.setDescripcion(nuevo.getDescripcion());
        h.setRol(nuevo.getRol());
        h.setCompletado(nuevo.isCompletado());

        Habito actualizado = habitoRepository.save(h);
        System.out.println("Hábito actualizado exitosamente");

        return ResponseEntity.ok(actualizado);
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
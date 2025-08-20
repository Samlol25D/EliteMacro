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

@RestController
@RequestMapping("/api/habitos")
@CrossOrigin(origins = "*")
public class HabitoController {

    @Autowired
    private HabitoRepository habitoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public List<Habito> listarHabitos() {
        return habitoRepository.findAll();
    }

    @PostMapping
    public Habito crearHabito(@RequestBody Habito habito) {
        return habitoRepository.save(habito);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habito> obtenerHabito(@PathVariable Long id) {
        return habitoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/mis-habitos")
    public List<Habito> listarMisHabitos(Authentication auth) {
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);
        return habitoRepository.findByUsuario(usuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarHabito(
            @PathVariable Long id,
            @RequestBody Habito nuevo,
            Authentication auth) {

        return habitoRepository.findById(id).map(h -> {
            // Verificar permisos: dueño o administrador
            String username = auth.getName();
            boolean esDuenio = h.getUsuario().getUsername().equals(username);
            boolean esAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!esDuenio && !esAdmin) {
                return ResponseEntity.status(403).body("No tienes permisos para editar este hábito");
            }

            // Actualizar campos permitidos
            h.setNombre(nuevo.getNombre());
            h.setDescripcion(nuevo.getDescripcion());
            h.setRol(nuevo.getRol());
            h.setCompletado(nuevo.isCompletado());

            return ResponseEntity.ok(habitoRepository.save(h));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarHabito(
            @PathVariable Long id,
            Authentication auth) {

        return habitoRepository.findById(id).map(h -> {
            // Verificar permisos: dueño o administrador
            String username = auth.getName();
            boolean esDuenio = h.getUsuario().getUsername().equals(username);
            boolean esAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!esDuenio && !esAdmin) {
                return ResponseEntity.status(403).body("No tienes permisos para eliminar este hábito");
            }

            habitoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
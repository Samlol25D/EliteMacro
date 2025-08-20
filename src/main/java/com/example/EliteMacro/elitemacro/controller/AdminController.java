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

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HabitoRepository habitoRepository;


    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    @GetMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/usuarios/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cambiarEstadoUsuario(@PathVariable Long id, @RequestParam boolean activo) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setActivo(activo);
            usuarioRepository.save(usuario);
            return ResponseEntity.ok("Estado actualizado a " + (activo ? "activo" : "inactivo"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/habitos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Habito> crearHabitoPredeterminado(@RequestBody Habito habito) {
        Habito nuevo = habitoRepository.save(habito);
        return ResponseEntity.ok(nuevo);
    }

    @GetMapping("/habitos")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Habito> listarHabitosAdmin() {
        return habitoRepository.findAll();
    }
    @PutMapping("/habitos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Habito> editarHabito(@PathVariable Long id, @RequestBody Habito habitoActualizado) {
        return habitoRepository.findById(id).map(h -> {
            h.setNombre(habitoActualizado.getNombre());
            h.setDescripcion(habitoActualizado.getDescripcion());
            h.setRol(habitoActualizado.getRol());
            return ResponseEntity.ok(habitoRepository.save(h));
        }).orElse(ResponseEntity.notFound().build());
    }
}

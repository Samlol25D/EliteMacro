package com.example.EliteMacro.elitemacro.controller;


import com.example.EliteMacro.elitemacro.model.Usuario;
import com.example.EliteMacro.elitemacro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registro")
@CrossOrigin(origins = "*")
public class RegistroController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<String> registrar(@RequestBody Usuario usuario) {
        if (usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("El usuario ya existe");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRol("ROLE_USER"); // todos los registrados serán USER
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Usuario registrado correctamente");
    }
}

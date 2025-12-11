package com.example.EliteMacro.elitemacro.service;

import com.example.EliteMacro.elitemacro.model.Usuario;
import com.example.EliteMacro.elitemacro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioService {

    private static UsuarioRepository usuarioRepository;

    // Inyección por constructor (RECOMENDADO)
    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository) {
        UsuarioService.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public static void agregarExperiencia(String username, int experiencia) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(username);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            int nivelAnterior = usuario.getNivel();

            usuario.agregarExperiencia(experiencia);
            usuarioRepository.save(usuario);

            // Verificar si subió de nivel
            if (usuario.getNivel() > nivelAnterior) {
                System.out.println("¡" + username + " subió al nivel " + usuario.getNivel() + "!");
            }
        }
    }

    public Optional<Usuario> obtenerUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public Usuario guardarUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public long contarAdmins() {
        return usuarioRepository.countByRol("ROLE_ADMIN");
    }
}
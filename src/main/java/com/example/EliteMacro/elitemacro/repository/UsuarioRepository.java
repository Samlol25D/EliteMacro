package com.example.EliteMacro.elitemacro.repository;

import com.example.EliteMacro.elitemacro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    long countByRol(String rol);

    // Método con @Query (opcional)
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol = :rol")
    long contarPorRol(@Param("rol") String rol);
}
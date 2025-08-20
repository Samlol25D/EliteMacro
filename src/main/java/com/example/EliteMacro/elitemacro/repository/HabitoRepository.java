package com.example.EliteMacro.elitemacro.repository;


import com.example.EliteMacro.elitemacro.model.Habito;
import com.example.EliteMacro.elitemacro.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HabitoRepository extends JpaRepository<Habito, Long> {
    List<Habito> findByUsuario(Usuario usuario);

}

package com.example.EliteMacro.elitemacro.controller;

import com.example.EliteMacro.elitemacro.model.Usuario;
import com.example.EliteMacro.elitemacro.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/usuario")
    public String getUsuarioActual(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "Invocador";
    }

    @GetMapping("/usuario-info")
    public ResponseEntity<?> getUsuarioInfo(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", auth.getName());
            userInfo.put("roles", auth.getAuthorities().toString());
            userInfo.put("isAdmin", auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            return ResponseEntity.ok(userInfo);
        }
        return ResponseEntity.status(401).build();
    }

    // NUEVO ENDPOINT: Información completa con niveles y experiencia
    @GetMapping("/usuario/info-completa")
    public ResponseEntity<?> getInfoCompleta(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorUsername(username);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioOpt.get();

        Map<String, Object> info = new HashMap<>();
        info.put("username", usuario.getUsername());
        info.put("nivel", usuario.getNivel());
        info.put("rango", usuario.getRango());
        info.put("experienciaTotal", usuario.getExperienciaTotal());
        info.put("experienciaActual", usuario.getExperienciaActual());
        info.put("experienciaParaSiguienteNivel", usuario.getExperienciaParaSiguienteNivel());
        info.put("porcentajeProgreso", usuario.getPorcentajeProgreso());
        info.put("isAdmin", usuario.getRol().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(info);
    }

    // NUEVO ENDPOINT: Estadísticas para el panel de usuario
    @GetMapping("/usuario/estadisticas")
    public ResponseEntity<?> getEstadisticas(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorUsername(username);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioOpt.get();

        Map<String, Object> stats = new HashMap<>();
        stats.put("nivel", usuario.getNivel());
        stats.put("rango", usuario.getRango());
        stats.put("experienciaTotal", usuario.getExperienciaTotal());
        stats.put("experienciaActual", usuario.getExperienciaActual());
        stats.put("experienciaParaSiguienteNivel", usuario.getExperienciaParaSiguienteNivel());
        stats.put("progresoNivel", String.format("%.1f%%", usuario.getPorcentajeProgreso()));
        stats.put("siguienteNivel", usuario.getNivel() + 1);
        stats.put("expFaltante", usuario.getExperienciaParaSiguienteNivel() - usuario.getExperienciaActual());
        stats.put("porcentajeProgresoNumero", usuario.getPorcentajeProgreso());

        return ResponseEntity.ok(stats);
    }

    // NUEVO ENDPOINT: Solo nivel y rango (para mostrar en header)
    @GetMapping("/usuario/nivel")
    public ResponseEntity<?> getNivelUsuario(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("No autenticado");
        }

        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioService.obtenerUsuarioPorUsername(username);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioOpt.get();

        Map<String, Object> nivelInfo = new HashMap<>();
        nivelInfo.put("nivel", usuario.getNivel());
        nivelInfo.put("rango", usuario.getRango());
        nivelInfo.put("experienciaTotal", usuario.getExperienciaTotal());

        return ResponseEntity.ok(nivelInfo);
    }
}
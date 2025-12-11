package com.example.EliteMacro.elitemacro.config;

import com.example.EliteMacro.elitemacro.model.Usuario;
import com.example.EliteMacro.elitemacro.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;  // Instancia inyectada

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== INICIALIZANDO DATOS ===");

        // ✅ CORRECTO: Usar la instancia 'usuarioRepository' (con 'u' minúscula)
        if (!usuarioRepository.findByUsername("admin").isPresent()) {
            System.out.println("👑 Creando usuario administrador...");

            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("Admin123!")); // Contraseña: Admin123!
            admin.setEmail("admin@elitemacro.com");
            admin.setRol("ROLE_ADMIN");
            admin.setActivo(true);
            admin.setExperienciaTotal(5000);
            admin.setNivel(25);
            admin.setExperienciaActual(1200);
            admin.setExperienciaParaSiguienteNivel(2000);

            // ✅ CORRECTO: Usar la instancia
            usuarioRepository.save(admin);

            System.out.println("✅ ADMIN CREADO EXITOSAMENTE");
            System.out.println("📋 Credenciales:");
            System.out.println("   Usuario: admin");
            System.out.println("   Contraseña: Admin123!");
            System.out.println("   Email: admin@elitemacro.com");
            System.out.println("🔗 Accede en: https://elitemacro.onrender.com/login.html");
        } else {
            System.out.println("✅ Usuario admin ya existe");
        }

        System.out.println("=== INICIALIZACIÓN COMPLETADA ===\n");
    }
}
package com.example.EliteMacro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EliteMacroApplication {

	public static void main(String[] args) {
		// FORZAR perfil 'dev' si no hay otro especificado
		if (System.getProperty("spring.profiles.active") == null &&
				System.getenv("SPRING_PROFILES_ACTIVE") == null) {

			System.setProperty("spring.profiles.active", "dev");
			System.out.println("⚠️ No hay perfil activo, forzando perfil 'dev'");
		}

		SpringApplication.run(EliteMacroApplication.class, args);
	}
}
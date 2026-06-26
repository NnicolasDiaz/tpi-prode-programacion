package com.example.demo.security;

import com.example.demo.models.Rol;
import com.example.demo.models.Usuario;
import com.example.demo.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        migrateEstadoPartidoConstraint();
        createAdminIfAbsent();
    }

    private void migrateEstadoPartidoConstraint() {
        try {
            jdbcTemplate.execute("ALTER TABLE partidos DROP CONSTRAINT IF EXISTS partidos_estado_check");
            jdbcTemplate.execute(
                "ALTER TABLE partidos ADD CONSTRAINT partidos_estado_check " +
                "CHECK (estado IN ('POR_JUGARSE','APLAZADO','EN_JUEGO','FINALIZADO'))"
            );
        } catch (Exception e) {
            System.out.println("⚠️  Migración constraint estado: " + e.getMessage());
        }
    }

    private void createAdminIfAbsent() {
        if (!usuarioRepository.existsByEmail("admin@prode.com")) {
            Usuario admin = Usuario.builder()
                    .nombre("Admin")
                    .apellido("Sistema")
                    .email("admin@prode.com")
                    .contrasena(passwordEncoder.encode("admin123"))
                    .rol(Rol.ADMINISTRADOR)
                    .build();
            usuarioRepository.save(admin);
            System.out.println("✅ Admin creado → email: admin@prode.com  /  contraseña: admin123");
        }
    }
}

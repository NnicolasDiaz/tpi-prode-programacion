package com.example.demo.security;

import com.example.demo.models.Rol;
import com.example.demo.models.Usuario;
import com.example.demo.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
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

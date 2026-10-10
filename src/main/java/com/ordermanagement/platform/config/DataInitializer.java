package com.ordermanagement.platform.config;

import com.ordermanagement.platform.auth.entity.User;
import com.ordermanagement.platform.auth.enums.Role;
import com.ordermanagement.platform.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer
        implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        String adminEmail = "admin@ordermanagement.com";

        // Verifica si el administrador ya existe.
        if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
            return;
        }

        // Crea el usuario administrador.
        User admin = User.builder()
                .name("Administrador")
                .email(adminEmail)
                .password(passwordEncoder.encode("Admin12345"))
                .role(Role.ADMIN)
                .active(true)
                .build();

        userRepository.save(admin);

        System.out.println("==========================================");
        System.out.println("ADMIN DE DESARROLLO CREADO");
        System.out.println("Email: " + adminEmail);
        System.out.println("Password: Admin12345");
        System.out.println("==========================================");
    }
}
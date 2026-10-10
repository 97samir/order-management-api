package com.ordermanagement.platform.auth.service;

import com.ordermanagement.platform.auth.dto.AuthResponse;
import com.ordermanagement.platform.auth.dto.LoginRequest;
import com.ordermanagement.platform.auth.dto.RegisterRequest;
import com.ordermanagement.platform.auth.entity.User;
import com.ordermanagement.platform.auth.enums.Role;
import com.ordermanagement.platform.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException(
                    "El email ya está registrado"
            );
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(request.email().trim().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmailIgnoreCase(request.email())
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Email o contraseña incorrectos"
                        )
                );

        if (!user.getActive()) {
            throw new IllegalStateException(
                    "El usuario se encuentra inactivo"
            );
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new BadCredentialsException(
                    "Email o contraseña incorrectos"
            );
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
package com.ordermanagement.platform.auth.controller;

import com.ordermanagement.platform.auth.dto.AuthResponse;
import com.ordermanagement.platform.auth.dto.LoginRequest;
import com.ordermanagement.platform.auth.dto.RegisterRequest;
import com.ordermanagement.platform.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication",
        description = "Operaciones de autenticación y registro de usuarios")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar usuario",
            description = "Registra un nuevo usuario en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de registro inválidos"),
            @ApiResponse(responseCode = "409", description = "El usuario ya existe")
    })
    @SecurityRequirements // Este endpoint es público, no necesita Bearer JWT
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return authService.register(request);
    }

    @Operation(summary = "Iniciar sesión",
            description = "Autentica un usuario y genera un token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso"),
            @ApiResponse(responseCode = "400", description = "Datos de inicio de sesión inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    @SecurityRequirements // Este endpoint es público, no necesita Bearer JWT
    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }
}
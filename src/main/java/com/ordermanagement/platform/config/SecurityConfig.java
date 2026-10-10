package com.ordermanagement.platform.config;

import com.ordermanagement.platform.auth.security.JwtAccessDeniedHandler;
import com.ordermanagement.platform.auth.security.JwtAuthenticationEntryPoint;
import com.ordermanagement.platform.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                .authorizeHttpRequests(auth -> auth

                        // AUTENTICACIÓN
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // SWAGGER / OPENAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // CATEGORÍAS
                        .requestMatchers(HttpMethod.GET,
                                "/api/categories/**"
                        ).hasAnyRole("ADMIN", "CUSTOMER")

                        .requestMatchers(HttpMethod.POST,
                                "/api/categories/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/categories/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE,
                                "/api/categories/**"
                        ).hasRole("ADMIN")

                        // PRODUCTOS
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/**"
                        ).hasAnyRole("ADMIN", "CUSTOMER")

                        .requestMatchers(HttpMethod.POST,
                                "/api/products/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/products/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE,
                                "/api/products/**"
                        ).hasRole("ADMIN")

                        // CLIENTES
                        .requestMatchers(
                                "/api/customers/**"
                        ).hasRole("ADMIN")

                        // PEDIDOS
                        .requestMatchers(HttpMethod.POST,
                                "/api/orders"
                        ).hasAnyRole("ADMIN", "CUSTOMER")

                        .requestMatchers(HttpMethod.GET,
                                "/api/orders/**"
                        ).hasAnyRole("ADMIN", "CUSTOMER")

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/orders/*/confirm"
                        ).hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/orders/*/cancel"
                        ).hasAnyRole("ADMIN", "CUSTOMER")

                        .requestMatchers(HttpMethod.PATCH,
                                "/api/orders/*/complete"
                        ).hasRole("ADMIN")

                        // RESTO
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
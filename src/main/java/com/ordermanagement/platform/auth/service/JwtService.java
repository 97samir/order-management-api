package com.ordermanagement.platform.auth.service;

import com.ordermanagement.platform.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expiration;

    // obtiene valor de application.yml
    public JwtService(
            @Value("${spring.jwt.secret}") String secret,
            @Value("${spring.jwt.expiration}") long expiration
    ) {

        // convierte el formatio BASE64  a un arreglo de bytes, y contruir clave criptografica
        byte[] keyBytes = Decoders.BASE64.decode(secret);

        // convierte a secretKey válida para utilziarla con HMAC
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        // se obtiene el valor del tiempo
        this.expiration = expiration;
    }

    public String generateToken(User user) {

        Date now = new Date();

        Date expirationDate = new Date(
                now.getTime() + expiration
        );

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(signingKey)
                .compact();
    }

    public String extractEmail(String token) {

        return extractAllClaims(token)
                .getSubject();
    }

    public boolean isTokenValid(String token) {

        try {
            // extrae informacion del jwt
            Claims claims = extractAllClaims(token);

            return claims.getExpiration()
                    .after(new Date());

        } catch (Exception exception) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {

        // crea un parser encargador de interpretar el JWT
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractRole(String token) {

        return extractAllClaims(token)
                .get("role", String.class);
    }
}
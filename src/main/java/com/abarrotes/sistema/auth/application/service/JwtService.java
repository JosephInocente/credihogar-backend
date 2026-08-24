package com.abarrotes.sistema.auth.application.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    // IMPORTANTE: En producción esta clave debe ir en variables de entorno (RNF24)
    // Para desarrollo usaremos esta clave estática de 256 bits (Base64)
    private static final String SECRET_KEY_DEV = "4qhq8LrEBfYcaRHxhdb9zURb2rf8e7UdG6mHJmO8a2M=";

    // Tiempo de expiración: 24 horas en milisegundos (RNF23)
    private static final long EXPIRATION_TIME = 86400000;

    public String generarToken(String username, String rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", rol); // Guardamos el rol dentro del token

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY_DEV);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
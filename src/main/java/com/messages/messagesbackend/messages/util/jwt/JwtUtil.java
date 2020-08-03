package com.messages.messagesbackend.messages.util.jwt;

import com.messages.messagesbackend.messages.dto.JwtDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

public class JwtUtil {
    private final SecretKey secretKey;

    public JwtUtil(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public JwtDto generateJwtDto(String string) {
        return new JwtDto(generateBearerToken(string));
    }

    public String generateBearerToken(String subject) {
        return "Bearer " + generateCompactedToken(subject);
    }

    public String generateCompactedToken(String subject) {
        return generateToken(subject, Duration.ofMinutes(10)).compact();
    }

    public JwtBuilder generateToken(String subject, Duration expiresAfter) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new java.sql.Date(System.currentTimeMillis() + expiresAfter.toMillis()))
                .signWith(secretKey);
    }

    // parseClaimsJws() verifies
    public String getSubjectAndVerify(String jws) {
        return Jwts
                .parserBuilder()
                    .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(jws)
                .getBody()
                .getSubject();
    }

    public Jws<Claims> extract(String jws) {
        return Jwts
                .parserBuilder()
                    .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(jws);
    }
}
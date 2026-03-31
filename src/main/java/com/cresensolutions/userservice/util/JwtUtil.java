package com.cresensolutions.userservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private final String SecretKey = "secretkey@123!";

    public String generateToken(String username, String role){
        return Jwts.builder()
                .setSubject(username)
                .claim("role",role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(SignatureAlgorithm.HS256, SecretKey)
                .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SecretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    // Extracting username
    public String extractUsername(String token){
        return getClaims(token).getSubject();
    }

    // Extracting role
    public String extractRole(String token){
        return (String) getClaims(token).get("role");
    }

    // Validate token (Expiry check)
    public boolean validateToken(String token){
        return getClaims(token).getExpiration().after(new Date());
    }

}

package com.job.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkey"; // must be at least 32 chars
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String username) {
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();

        System.out.println("✅ Generated Token for user [" + username + "]: " + token);
        System.out.println("🧪 Secret Key (debug): " + SECRET_KEY);
        System.out.println("📦 Incoming token: " + token);

        return token;
    }

    public String extractUsername(String token) {
        try {
            String username = getClaims(token).getSubject();
            System.out.println("✅ Extracted username from token: " + username);
            return username;
        } catch (Exception e) {
            System.out.println("❌ Failed to extract username: " + e.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token); // will throw if invalid
            System.out.println("✅ Token is valid");
            return true;
        } catch (Exception e) {
            System.out.println("❌ Invalid token: " + e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

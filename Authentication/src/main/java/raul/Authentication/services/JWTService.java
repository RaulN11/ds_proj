package raul.Authentication.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import raul.Authentication.entities.UserCredentials;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JWTService {
    private final SecretKey key;
    private final long expirationTimeMs;
    public JWTService(@Value("${JWT_SECRET}")String secret, @Value("${JWT_EXPIRATIONTIMEMS}") long expirationTimeMs){
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationTimeMs = expirationTimeMs;
    }
    public String generate(UserCredentials userCredentials) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userCredentials.getUsername())
                .claim("id", userCredentials.getId().toString())
                .claim("role", userCredentials.getRole())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationTimeMs))
                .signWith(key)
                .compact();
    }
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }
}

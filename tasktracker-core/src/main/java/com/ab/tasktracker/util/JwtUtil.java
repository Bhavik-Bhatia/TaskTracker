package com.ab.tasktracker.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.impl.security.StandardSecureDigestAlgorithms;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@NoArgsConstructor
@AllArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String SECRET;
    @Value("${jwt.expiration.time}")
    private long EXPIRATION_TIME;

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);

    public String generateJWTToken(String email, Long userID) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userID)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), StandardSecureDigestAlgorithms.findBySigningKey(getSigningKey()))
                .compact();
    }

    public String extractUsername(String token) {
        try {
            String username = Jwts
                    .parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            LOGGER.debug("JwtUtil.extractUsername result - " + username);
            return username;
        } catch (MalformedJwtException e) {
            LOGGER.error("Exception in JwtUtil.extractUsername");
            throw e;
        }
    }

    /**
     * Checks if token is expired or not
     *
     * @param token token is set
     * @return boolean
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            LOGGER.error("Exception in JwtUtil.isTokenExpired : Token is expired");
            return true;
        }
    }

    /**
     * Extracts all claims
     *
     * @param token token is set
     * @return
     */
    public Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}

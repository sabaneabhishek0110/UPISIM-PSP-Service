package com.example.psp.service;

import com.example.psp.model.User;
import com.example.psp.security.KeyManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    private final KeyManager keyManager;


    public TokenService(KeyManager keyManager) throws Exception {
        this.keyManager = keyManager; // path in resources
    }

    public String issueToken(String userId) throws Exception{
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(7 * 24 * 60 * 60); // 15 min
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .setIssuer("auth-service")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("scope", "openid")
                .setHeaderParam("kid", keyManager.getKid())
                .signWith(keyManager.getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }
    public String validateToken(String token) throws Exception{
        Claims claims =  Jwts.parser()
                .setSigningKey(keyManager.getPublicKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

}

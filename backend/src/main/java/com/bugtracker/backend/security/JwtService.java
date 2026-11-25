package com.bugtracker.backend.security;

import com.bugtracker.backend.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expiration;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.expiration}") long expiration
    ) {
        this.jwtEncoder = jwtEncoder;
        this.expiration = expiration;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("bugtracker")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiration))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
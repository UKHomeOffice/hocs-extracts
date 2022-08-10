package uk.gov.digital.ho.hocs.extracts.core.utils;

import io.jsonwebtoken.Jwts;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.jsonwebtoken.SignatureAlgorithm.RS256;

@Component
@Profile("test")
public class JwtAuthHelper {
    private final KeyPair keyPair;

    public JwtAuthHelper() throws NoSuchAlgorithmException {
        var gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        keyPair = gen.generateKeyPair();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
    }

    public String createJwt(String subject, List<String> roles) {
        var claims = Map.of("clientId", subject, "realm_access", Map.of("roles", roles));
        return Jwts.builder()
                .addClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + Duration.ofHours(1).toMillis()))
                .setId(UUID.randomUUID().toString())
                .setSubject(subject)
                .signWith(RS256, keyPair.getPrivate())
                .compact();
    }
}

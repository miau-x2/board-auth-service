package com.example.board.auth.authentication.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class JwtKeyConfig {
    private final TokenProperties tokenProperties;

    @Bean
    public PrivateKey privateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        return PemUtils.getPrivateKey(tokenProperties.access().jwt().keys().privateKeyPem());
    }

    @Bean
    public PublicKey publicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        return PemUtils.getPublicKey(tokenProperties.access().jwt().keys().publicKeyPem());
    }
}

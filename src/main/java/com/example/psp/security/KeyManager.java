package com.example.psp.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Component
public class KeyManager {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final String kid;

//    public KeyManager() throws Exception {
////        this.privateKey = KeyUtils.loadPrivateKey("keys/psp-auth/private.pem"); // resource path
////        this.publicKey = KeyUtils.loadPublicKey("keys/psp-auth/public.pem");
////        this.kid = KeyUtils.computeKid(publicKey);
//    }

    public KeyManager(
            @Value("${security.psp-auth-private-key}") String privateKeyPem,
            @Value("${security.psp-auth-public-key}") String publicKeyPem
    ) throws Exception {
        this.privateKey = KeyUtils.loadPrivateKey(privateKeyPem);
        this.publicKey = KeyUtils.loadPublicKey(publicKeyPem);
        this.kid = KeyUtils.computeKid(publicKey);
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public String getKid() {
        return kid;
    }
}
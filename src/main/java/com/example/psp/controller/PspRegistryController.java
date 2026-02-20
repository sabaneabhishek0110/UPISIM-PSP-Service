package com.example.psp.controller;

import com.example.psp.dto.PspPublicKeyRequestToNpci;
import com.example.psp.dto.PspRegistryRequestToPsp;
import com.example.psp.service.PSPService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.security.PublicKey;

@RestController
@RequestMapping("/internal/psp/npci")
public class PspRegistryController {
    private final PSPService pspService;

    @Value("${url.npci_url}")
    private String NPCI_URL;

    public PspRegistryController(PSPService pspService) {
        this.pspService = pspService;
    }

    @PostMapping("/register-public-key")
    public ResponseEntity<String> PspRegistry(@RequestBody PspRegistryRequestToPsp requestBody) throws Exception{
        RestTemplate restTemplate = new RestTemplate();

        PublicKey publicKey = pspService.loadPublicKey();
        String publicKeyBase64 = pspService.publicKeyToBase64(publicKey);
        PspPublicKeyRequestToNpci request = new PspPublicKeyRequestToNpci();
        request.setPspCode(requestBody.getPspCode());
        request.setPublicKey(publicKeyBase64);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PspPublicKeyRequestToNpci> entity =
                new HttpEntity<>(request, headers);

        String url = NPCI_URL + "/internal/npci/psp/register-public-key";

        String response = restTemplate.postForObject(
                url,
                entity,
                String.class
        );
        return ResponseEntity.ok(response);
    }
}

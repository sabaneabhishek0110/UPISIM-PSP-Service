package com.example.psp.controller;

import com.example.psp.config.RestTemplateConfig;
import com.example.psp.dto.HealthRequest;
import com.example.psp.dto.PaymentRequest;
import com.example.psp.dto.PaymentResponse;
import com.example.psp.service.PSPService;
import com.example.psp.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@RestController
@RequestMapping
public class HealthController {

    @Value("${url.npci_url}")
    private String NPCI_URL;

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private PSPService pspService;

    public HealthController(RestTemplate restTemplate,ObjectMapper objectMapper,PSPService pspService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.pspService = pspService;
    }

    @GetMapping("/health")
    public String health() throws Exception{
        HealthRequest Req = new HealthRequest("PHONEPE");

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        String payload = objectMapper.writeValueAsString(Req);
        String signature = pspService.signPayload(payload);
        headers.set("X-PSP-CODE","PHONEPE");
        headers.set("X-SIGNATURE",signature);
        headers.add("X-TIMESTAMP", Instant.now().toString());
        HttpEntity<HealthRequest> entity = new HttpEntity<>(Req, headers);
        String url = NPCI_URL + "/health";
        String response = restTemplate.postForObject(
                url,
                entity,
                String.class
        );
        System.out.println("Response from NPCI : "+response);

        return "PSP Service Is Now Active!!!";
    }
}

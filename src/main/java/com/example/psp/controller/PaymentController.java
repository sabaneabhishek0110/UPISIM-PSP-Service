package com.example.psp.controller;

import com.example.psp.dto.PaymentInitiateRequest;
import com.example.psp.dto.PaymentRequest;
import com.example.psp.dto.PaymentResponse;
import com.example.psp.model.PSP_TRANSACTIONS;
import com.example.psp.service.PSPService;
import com.example.psp.service.PaymentService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentService paymentService;
    private final PSPService pspService;
    private final RestTemplate restTemplate;

    @Value("${url.npci_url}")
    private String NPCI_URL;

    public PaymentController(
            PaymentService paymentService,
            PSPService pspService,
            RestTemplate restTemplate) {
        this.paymentService = paymentService;
        this.pspService = pspService;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@RequestBody PaymentInitiateRequest requestBody, HttpServletRequest request) {
        String PayerVpa = (String) request.getAttribute("vpa");
        String PayeeVpa = requestBody.getPayeeVpa();
        Double amount = requestBody.getAmount();
        String pin = requestBody.getPin();
        String message = requestBody.getMessage();
        System.out.println("PayerVpa : "+PayerVpa);
        System.out.println("PayeeVpa : "+PayeeVpa);
        String psp_txn_id = paymentService.createPspTransaction(PayerVpa,PayeeVpa,amount,message);
//        System.out.println("Payee VPA: " + PayeeVpa);
        HttpEntity<PaymentRequest> entity = paymentService.createPaymentRequest(PayerVpa, PayeeVpa, amount,pin,psp_txn_id);
        System.out.println("......... sending to npci");

        String url = NPCI_URL + "/api/payment/process";
        PaymentResponse response = restTemplate.postForObject(
                url,
                entity,
                PaymentResponse.class
        );
        paymentService.handlePaymentResponse(response);
        if(response.getResponse_code().equals("U03")){
            return ResponseEntity.status(404).body(response);
        }
        else if(response.getResponse_code().equals("U02")){
            return ResponseEntity.status(403).body(response);
        }
        else if(response.getResponse_code().equals("U01")){
            return ResponseEntity.status(401).body(response);
        }
        else if(response.getResponse_code().equals("U14")){
            return ResponseEntity.status(402).body(response);
        }
        else{
            return ResponseEntity.ok(response);
        }
    }
}

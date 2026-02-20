package com.example.psp.controller;

import com.example.psp.dto.*;
import com.example.psp.model.PSP_TRANSACTIONS;
import com.example.psp.service.PSPService;
import com.example.psp.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.security.PrivateKey;
import java.security.Signature;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final PSPService pspService;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private RestTemplate restTemplate;

    @Value("${url.npci_url}")
    private String NPCI_URL;

    public  AccountController(PSPService pspService,ObjectMapper objectMapper,RestTemplate restTemplate,PaymentService paymentService) {
        this.pspService = pspService;
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/balance")
    public ResponseEntity<?> checkBalance(@RequestBody BalanceRequestToPsp requestBody){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || auth.getPrincipal()==null || auth.getPrincipal().equals("anonymousUser")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try{
            String pin = requestBody.getPin();
            UUID userId = auth.getName()!=null ? UUID.fromString(auth.getName()) : null;
            UserDTO user = pspService.getUserById(userId);

            BalanceRequest request = new BalanceRequest(user.getVpa(),pin);

            String payload = objectMapper.writeValueAsString(request);

            String signature = pspService.signPayload(payload);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-TIMESTAMP", Instant.now().toString());
            headers.set("X-SIGNATURE",signature);
            headers.set("X-PSP-CODE","PHONEPE");

            HttpEntity<BalanceRequest> entity = new HttpEntity<>(request,headers);

            System.out.println("Sending to npci");

            String url = NPCI_URL + "/api/account/balance";
            BalanceResponse response = restTemplate.postForObject(
                    url,
                    entity,
                    BalanceResponse.class
            );
            System.out.println("response : "+response);
            if(response.getResponseCode().equals("U01")){
                return ResponseEntity.status(401).body(response);
            }
            else{
                return ResponseEntity.ok(response);
            }
        }
        catch(Exception e){
            return ResponseEntity.internalServerError().body("Failed to fetch balance");
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(@RequestParam Integer limit, @RequestParam Integer offset){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || auth.getPrincipal()==null || auth.getPrincipal().equals("anonymousUser")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try{
            System.out.println("Entered in getTransactions");
            UUID userId = auth.getName()!=null ? UUID.fromString(auth.getName()) : null;
            String vpa = pspService.getUserVpa(userId.toString());

            List<PSP_TRANSACTIONS> transactions = paymentService.findTransactions(vpa,limit,offset);

            System.out.println("Transactions : "+transactions);
            TransactionsResponse response = new TransactionsResponse();
            response.setTransactions(transactions);
            return ResponseEntity.ok(response);
        }
        catch(Exception e){
            return ResponseEntity.internalServerError().body("Failed to fetch transactions");
        }

    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> getTransactionDetails(@PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || auth.getPrincipal()==null || auth.getPrincipal().equals("anonymousUser")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try{
            System.out.println("Entered in getTransactionDetails");
            UUID userId = auth.getName()!=null ? UUID.fromString(auth.getName()) : null;

            PSP_TRANSACTIONS transaction = paymentService.findTransactionById(id);

            System.out.println("Transaction : "+transaction);
            TransactionsDetailsResponse response = new TransactionsDetailsResponse();
            response.setTransaction(transaction);
            return ResponseEntity.ok(response);
        }
        catch(Exception e){
            return ResponseEntity.internalServerError().body("Failed to fetch transaction details");
        }

    }
}

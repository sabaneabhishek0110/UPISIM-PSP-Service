package com.example.psp.controller;

import com.example.psp.dto.*;
import com.example.psp.model.PSP_TRANSACTIONS;
import com.example.psp.service.PSPService;
import com.example.psp.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@Slf4j
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
    public ResponseEntity<?> initiatePayment(
            @RequestBody PaymentInitiateRequest requestBody,
            HttpServletRequest request) {

        String payerVpa = (String) request.getAttribute("vpa");
        String payeeVpa = requestBody.getPayeeVpa();
        Double amount = requestBody.getAmount();
        String pin = requestBody.getPin();
        String message = requestBody.getMessage();

        // 1: Create PSP transaction (status = PROCESSING)
        String pspTxnId = paymentService.createPspTransaction(
                payerVpa, payeeVpa, amount, message
        );

        // 2: Create request to NPCI (includes callback URL)
        HttpEntity<PaymentRequest> entity =
                paymentService.createPaymentRequest(
                        payerVpa, payeeVpa, amount, pin, pspTxnId
                );

        try {
            String url = NPCI_URL + "/api/payment/process";
            restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            log.error("Error sending payment to NPCI for pspTxnId: {}. Error: {}", pspTxnId, e.getMessage());
            // Don't fail - the transaction is created, NPCI might still process it
            // Or we can mark it as failed if NPCI was unreachable
        }

        // 3: Return PROCESSING status immediately
        return ResponseEntity.ok(
                new PaymentResponse_1(
                        pspTxnId,
                        "PROCESSING",
                        "Payment is being processed"
                )
        );
    }

    @PostMapping("/callback")
    public ResponseEntity<?> handleCallback(
            @RequestBody CallbackRequest request) {
        log.info("Received callback for pspTxnId: {}, status: {}", request.getPspTxnId(), request.getStatus());
        paymentService.updateTransactionFromCallback(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Frontend polls this endpoint to check payment status
     */
    @GetMapping("/status/{pspTxnId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String pspTxnId) {
        try {
            PSP_TRANSACTIONS txn = paymentService.findTransactionById(pspTxnId);
            if (txn == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("pspTxnId", txn.getId().toString());
            response.put("payerVpa", txn.getPayer_vpa());
            response.put("payeeVpa", txn.getPayee_vpa());
            response.put("amount", txn.getAmount());
            response.put("status", txn.getStatus().name());
            response.put("upiTxnId", txn.getUpiTxnId());
            response.put("rrn", txn.getRrn());
            response.put("message", txn.getMessage());
            response.put("failureReason", txn.getFailure_reason());
            response.put("createdAt", txn.getCreatedAt());
            response.put("updatedAt", txn.getUpdatedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching payment status for pspTxnId: {}", pspTxnId, e);
            return ResponseEntity.notFound().build();
        }
    }
}

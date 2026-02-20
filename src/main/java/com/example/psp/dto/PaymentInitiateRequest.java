package com.example.psp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateRequest {
    private String payerVpa;
    private String payeeVpa;
    private Double amount;
    private String pin;
    private String message;
}


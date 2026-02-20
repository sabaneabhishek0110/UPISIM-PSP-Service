package com.example.psp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String payer_vpa;
    private String payee_vpa;
    private Double amount;
    private String psp_txn_id;
    private String upi_txn_id;
    private String rrn;
    private String status;
    private String response_code;
    private String failureReason;
}

/*
U03, //for account not found
U02, //for account is inactive
U01, //for wrong pin
U14, //for insufficient balance
U00  //for success
*/
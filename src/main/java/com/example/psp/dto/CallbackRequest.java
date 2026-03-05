package com.example.psp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CallbackRequest {

    private String pspTxnId;
    private String upiTxnId;
    private String rrn;
    private Double amount;
    private String status;   // SUCCESS / FAILED
    private String reason;   // only if failed
}

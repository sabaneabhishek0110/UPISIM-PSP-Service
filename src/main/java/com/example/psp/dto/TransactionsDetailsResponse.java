package com.example.psp.dto;

import com.example.psp.model.PSP_TRANSACTIONS;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsDetailsResponse {
    private PSP_TRANSACTIONS transaction;
}

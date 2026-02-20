package com.example.psp.dto;

import com.example.psp.model.PSP_TRANSACTIONS;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsResponse {
    private List<PSP_TRANSACTIONS> transactions;

    public List<PSP_TRANSACTIONS> getTransactions() {
        return transactions;
    }
    public void setTransactions(List<PSP_TRANSACTIONS> transactions) {
        this.transactions = transactions;
    }
}

package com.example.psp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "psp_transactions")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PSP_TRANSACTIONS {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payer_vpa", nullable = false)
    private String payer_vpa;

    @Column(name = "payee_vpa", nullable = false)
    private String payee_vpa;

    @Column(name = "upi_txn_id",nullable = true)
    private String upi_txn_id;

    @Column(name = "rrn",nullable = true)
    private String rrn;

    @Column(nullable = false)
    private Double amount; // transaction amount

    @Column(name="message")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status=TransactionStatus.PENDING;  // SUCCESS / FAILED / PENDING / REVERSED

    @Column(name = "failure_reason")
    private String failure_reason; // if status is FAILED, reason for failure

    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TransactionStatus {
        SUCCESS, FAILED, PENDING, REVERSED
    }

}



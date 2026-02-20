package com.example.psp.Repository;

import com.example.psp.model.PSP_TRANSACTIONS;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PspTransactionsRepository extends JpaRepository<PSP_TRANSACTIONS, UUID> {
    @Query("""
        SELECT t FROM PSP_TRANSACTIONS t 
        WHERE t.payer_vpa = :vpa OR t.payee_vpa = :vpa
        ORDER BY t.createdAt DESC ,t.id DESC
    """)
    List<PSP_TRANSACTIONS> findTransactionsByVpa(@Param("vpa") String vpa, Pageable pageable);

    Optional<PSP_TRANSACTIONS> findById(UUID uuid);
}

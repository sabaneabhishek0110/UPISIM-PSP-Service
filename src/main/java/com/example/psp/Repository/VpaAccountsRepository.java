package com.example.psp.Repository;

import com.example.psp.model.User;
import com.example.psp.model.VPA_ACCOUNT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VpaAccountsRepository extends JpaRepository<VPA_ACCOUNT, UUID> {
    @Query("SELECT a FROM VPA_ACCOUNT a WHERE a.user = :user")
    Optional<VPA_ACCOUNT> findVpaAccountByUser(@Param("user") User user);

    VPA_ACCOUNT findByVpa(String vpa);
}

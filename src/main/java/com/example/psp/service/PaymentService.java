package com.example.psp.service;

import com.example.psp.Repository.PspTransactionsRepository;
import com.example.psp.Repository.UserRepository;
import com.example.psp.Repository.VpaAccountsRepository;
import com.example.psp.dto.PaymentRequest;
import com.example.psp.dto.PaymentResponse;
import com.example.psp.model.PSP_TRANSACTIONS;
import com.example.psp.model.User;
import com.example.psp.model.VPA_ACCOUNT;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.io.IOException;


@Service
public class PaymentService {
    private final UserRepository userRepository;
    private final PspTransactionsRepository pspTransactionsRepository;
    private final PSPService pspService;
    private final ObjectMapper objectMapper;
    private final VpaAccountsRepository vpaAccountsRepository;
    public PaymentService(UserRepository userRepository,
                          PspTransactionsRepository pspTransactionsRepository,
                          PSPService pspService,
                          ObjectMapper objectMapper,
                          VpaAccountsRepository vpaAccountsRepository) {
        this.userRepository = userRepository;
        this.pspTransactionsRepository = pspTransactionsRepository;
        this.pspService = pspService;
        this.objectMapper = objectMapper;
        this.vpaAccountsRepository = vpaAccountsRepository;
    }

    public HttpEntity<PaymentRequest> createPaymentRequest(String PayerVpa,String PayeeVpa, Double amount,String pin,String psp_txn_id){
        try{

            PaymentRequest paymentRequest = new PaymentRequest(PayerVpa,PayeeVpa,amount,pin,psp_txn_id);

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_JSON);
            String payload = objectMapper.writeValueAsString(paymentRequest);
            String signature = pspService.signPayload(payload);
            headers.set("X-PSP-CODE","PHONEPE");
            headers.set("X-SIGNATURE",signature);
            headers.add("X-TIMESTAMP", Instant.now().toString());
            return new HttpEntity<>(paymentRequest, headers);
        }
        catch(Exception e){
            throw new IllegalStateException("failed to create Payment Request");
        }
    }

    public String createPspTransaction(String payerVpa,String payeeVpa, Double amount,String message) {
        PSP_TRANSACTIONS transaction = new PSP_TRANSACTIONS();
        transaction.setPayer_vpa(payerVpa);
        transaction.setPayee_vpa(payeeVpa);
        transaction.setAmount(amount);
        transaction.setMessage(message);
        transaction.setUpi_txn_id(null);
        transaction.setRrn(null);
        pspTransactionsRepository.save(transaction);
        return transaction.getId().toString();
    }

    public void handlePaymentResponse(PaymentResponse response) {
        UUID psp_txn_id = UUID.fromString(response.getPsp_txn_id());
        PSP_TRANSACTIONS transaction = pspTransactionsRepository.findById(psp_txn_id).get();
        if(response.getResponse_code().equals("U03")){
            transaction.setStatus(PSP_TRANSACTIONS.TransactionStatus.FAILED);
            transaction.setFailure_reason("Account Not Found");
            transaction.setUpi_txn_id(response.getUpi_txn_id());
            transaction.setRrn(null);
            pspTransactionsRepository.save(transaction);
        }
        else if(response.getResponse_code().equals("U02")){
            transaction.setStatus(PSP_TRANSACTIONS.TransactionStatus.FAILED);
            transaction.setFailure_reason("Account is Inactive");
            transaction.setUpi_txn_id(response.getUpi_txn_id());
            transaction.setRrn(null);
            pspTransactionsRepository.save(transaction);
        }
        else if(response.getResponse_code().equals("U01")){
            transaction.setStatus(PSP_TRANSACTIONS.TransactionStatus.FAILED);
            transaction.setFailure_reason("Wrong Pin Entered");
            transaction.setUpi_txn_id(response.getUpi_txn_id());
            transaction.setRrn(null);
            pspTransactionsRepository.save(transaction);
        }
        else if(response.getResponse_code().equals("U14")){
            transaction.setStatus(PSP_TRANSACTIONS.TransactionStatus.FAILED);
            transaction.setFailure_reason("Balance is Insufficient");
            transaction.setUpi_txn_id(response.getUpi_txn_id());
            transaction.setRrn(null);
            pspTransactionsRepository.save(transaction);
        }
        else{
            transaction.setStatus(PSP_TRANSACTIONS.TransactionStatus.SUCCESS);
            transaction.setFailure_reason(null);
            transaction.setUpi_txn_id(response.getUpi_txn_id());
            transaction.setRrn(response.getRrn());
            pspTransactionsRepository.save(transaction);
        }
    }

    public PSP_TRANSACTIONS findTransactionById(String psp_txn_id){
        UUID id_ =  UUID.fromString(psp_txn_id);
        Optional<PSP_TRANSACTIONS> transaction = pspTransactionsRepository.findById(id_);
        return transaction.get();
    }

    public List<PSP_TRANSACTIONS> findTransactions(String vpa,Integer limit,Integer offset){
        Pageable pageable = PageRequest.of(offset,limit); // PageRequest.of(PageNo,PageSize) -> PageNo : starting entry , PageSize : entries to be fetched
        System.out.println("Offset : "+offset);
        System.out.println("Limit : "+limit);
        List<PSP_TRANSACTIONS> transactions = pspTransactionsRepository.findTransactionsByVpa(vpa,pageable);
        return transactions;
    }

    public String findVpaByUserId(String user_id){
        UUID userId = UUID.fromString(user_id);
        User user = userRepository.findById(userId).get();
        VPA_ACCOUNT vpa_account = vpaAccountsRepository.findVpaAccountByUser(user).get();
        return vpa_account.getVpa();
    }
}

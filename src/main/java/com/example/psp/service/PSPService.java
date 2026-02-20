package com.example.psp.service;

import com.example.psp.Repository.UserRepository;
import com.example.psp.Repository.VpaAccountsRepository;
import com.example.psp.dto.UserDTO;
import com.example.psp.model.User;
import com.example.psp.model.VPA_ACCOUNT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
public class PSPService {
    private final UserRepository userRepository;
    private final VpaAccountsRepository vpaAccountsRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${security.psp-npci-private-key}")
    private String privateKey;

    @Value("${security.psp-npci-public-key}")
    private String publicKey;

    public PSPService(UserRepository userRepository,VpaAccountsRepository vpaAccountsRepository) {
        this.vpaAccountsRepository = vpaAccountsRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public User registerUser(String name,String phone, String password,String bankName) {
        Optional<User> existing = userRepository.findByPhone(phone);
        System.out.println("User.. : "+existing);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("phone already registered");
        }
        String hashPassword = hashPassword(password);
        String vpa = generateVpa(phone, bankName);
        User user = new User();
        user.setPhone(phone);
        user.setPassword(hashPassword);
        user.setName(name);

        System.out.println("User to be saved : "+user);
        User saved = userRepository.save(user);

        VPA_ACCOUNT vpa_account = new VPA_ACCOUNT();
        vpa_account.setBank(bankName);
        vpa_account.setVpa(vpa);
        vpa_account.setUser(user);
        vpaAccountsRepository.save(vpa_account);

        System.out.println("User : "+saved);

        return saved;
    }

    public String hashPassword(String Password) {
        return passwordEncoder.encode(Password);
    }

    public String generateVpa(String phoneNumber, String bankName) {
        return phoneNumber + "@" + bankName.toLowerCase();
    }

    public User authenticateUser(String phone, String password) {
        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Phone number not registered");
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("invalid phone or password");
        }
        return user;
    }

    public UserDTO getUserById(UUID userId) {
        User existuser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        VPA_ACCOUNT vpa_account = vpaAccountsRepository.findVpaAccountByUser(existuser).get();

        UserDTO user1 = new UserDTO(
                existuser.getId(),
                existuser.getName(),
                existuser.getPhone(),
                vpa_account.getVpa(),
                vpa_account.getBank()
        );

        return user1;
    }

    public void setUserPin(UUID userId, String pin) {
        System.out.println("Has to be implement");
    }

    public UserDTO getUserByVpa(String vpa) {
        VPA_ACCOUNT vpa_account = vpaAccountsRepository.findByVpa(vpa);

        User user = vpa_account.getUser();

        UserDTO user1 = new UserDTO(
                user.getId(),
                user.getPhone(),
                user.getName(),
                vpa_account.getVpa(),
                vpa_account.getBank()
        );

        return user1;
    }

    public VPA_ACCOUNT getVpaAccountByUser(User user) {
        return vpaAccountsRepository.findVpaAccountByUser(user).get();
    }

    public String getUserVpa(String userId) {
        UUID user_id = UUID.fromString(userId);
        User user = userRepository.findById(user_id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        VPA_ACCOUNT vpa_account = vpaAccountsRepository.findVpaAccountByUser(user).get();
        return vpa_account.getVpa();
    }

    public boolean userExits(String userId) {
        UUID id = UUID.fromString(userId);
        Optional<User> user = userRepository.findById(id);
        return user.isPresent();
    }

//    public PublicKey loadPublicKey() throws Exception {

//        InputStream is = getClass()
//                .getClassLoader()
//                .getResourceAsStream("keys/psp-npci/psp_npci_public.pem");

//        if (is == null) {
//            throw new RuntimeException("Public key file not found");
//        }

//        String publicKeyPem = new String(is.readAllBytes(), StandardCharsets.UTF_8);

//        publicKeyPem = publicKeyPem
//                .replace("-----BEGIN PUBLIC KEY-----", "")
//                .replace("-----END PUBLIC KEY-----", "")
//                .replaceAll("\\s", "");
//
//        byte[] decoded = Base64.getDecoder().decode(publicKeyPem);
//
//        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//
//        return keyFactory.generatePublic(keySpec);
//    }

    public PublicKey loadPublicKey() throws Exception {
        String publicKeyPem = publicKey;
         publicKeyPem = publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(publicKeyPem);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(keySpec);
    }


    public String publicKeyToBase64(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

//    PrivateKey loadPrivateKey() throws Exception {
//        InputStream is = getClass()
//                .getClassLoader()
//                .getResourceAsStream("keys/psp-npci/psp_npci_private.pem");
//
//        if (is == null) {
//            throw new RuntimeException("Public key file not found");
//        }
//
//        String privateKeyPem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
//
//        privateKeyPem = privateKeyPem.replace("-----BEGIN PRIVATE KEY-----", "")
//                .replace("-----END PRIVATE KEY-----", "")
//                .replaceAll("\\s", "");
//
//        byte[] decoded = Base64.getDecoder().decode(privateKeyPem);
//        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
//        KeyFactory kf = KeyFactory.getInstance("RSA");
//        return kf.generatePrivate(spec);
//    }


    PrivateKey loadPrivateKey() throws Exception {
        String privateKeyPem = privateKey;

        privateKeyPem = privateKeyPem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(privateKeyPem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public String signPayload(String payload) throws Exception {
        PrivateKey privateKey = loadPrivateKey();
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature.sign());
    }




}

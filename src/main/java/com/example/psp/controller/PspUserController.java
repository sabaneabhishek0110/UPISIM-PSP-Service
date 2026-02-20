package com.example.psp.controller;

import com.example.psp.dto.RegisterRequest;
import com.example.psp.dto.RegisterResponse;
import com.example.psp.dto.UserDTO;
import com.example.psp.model.User;
import com.example.psp.model.VPA_ACCOUNT;
import com.example.psp.service.PSPService;
import com.example.psp.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class PspUserController {
    private final PSPService pspService;
    private final TokenService tokenService;
    private static final String COOKIE_NAME = "token";

    public PspUserController(PSPService pspService, TokenService tokenService) {
        this.pspService = pspService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest req) {
        try{
            User user = pspService.registerUser(
                    req.getName(),
                    req.getPhone(),
                    req.getPassword(),
                    req.getBankName()
            );
            String token = tokenService.issueToken(user.getId().toString());
            ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME,token)
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(7*24*60*60)
                    .build();
            RegisterResponse response = new RegisterResponse(
              user.getId(),
              user.getName(),
              user.getPhone()
            );
            return  ResponseEntity
                    .ok()
                    .header("Set-Cookie",cookie.toString())
                    .body(response);
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().body(new RegisterResponse());
        }
        catch(Exception e) {
            return ResponseEntity.internalServerError().body(new RegisterResponse());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody RegisterRequest req) {
        try{
            User user = pspService.authenticateUser(req.getPhone(),req.getPassword());
            String token = tokenService.issueToken(user.getId().toString());
//            ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME,token)
//                    .httpOnly(true)
//                    .secure(false)
//                    .sameSite("Lax")
//                    .path("/")
//                    .maxAge(7*24*60*60)
//                    .build();


            //for production
            ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, token)
                    .httpOnly(true)
                    .secure(true)              // MUST be true in HTTPS
                    .sameSite("None")          // MUST be None for cross-site
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .build();

            VPA_ACCOUNT vpa_account = pspService.getVpaAccountByUser(user);
            UserDTO response = new UserDTO(
                user.getId(),
                user.getPhone(),
                user.getName(),
                vpa_account.getVpa(),
                vpa_account.getBank()
            );
            return  ResponseEntity
                    .ok()
                    .header("Set-Cookie",cookie.toString())
                    .body(response);
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UserDTO());
        }
        catch(Exception e) {
            return ResponseEntity.internalServerError().body(new UserDTO());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("status", "logged_out"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(HttpServletRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth==null || auth.getPrincipal()==null || auth.getPrincipal().equals("anonymousUser")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user not authenticated");
        }
        UUID userId = auth.getName()!=null ? UUID.fromString(auth.getName()) : null;
        UserDTO user = pspService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/set-pin")
    public ResponseEntity<?> setPin(@RequestBody String pin, HttpServletRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth==null || auth.getPrincipal()==null || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user not authenticated");
        }
        UUID userId = auth.getName()!=null ? UUID.fromString(auth.getName()) : null;
        try {
            pspService.setUserPin(userId, pin);
            return ResponseEntity.ok("PIN set successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error occured : "+e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to set PIN");
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserByVpa(@RequestParam(name="vpa",required = true) String vpa, HttpServletRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        System.out.println("Entered in getuserbyvpa");
        if(auth==null || auth.getPrincipal()==null || auth.getPrincipal().equals("anonymousUser")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("user not authenticated");
        }
        try {
            UserDTO user = pspService.getUserByVpa(vpa);
//            System.out.println("fetched user");
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error occured : "+e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch user by VPA");
        }
    }

}

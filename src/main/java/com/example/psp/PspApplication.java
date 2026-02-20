package com.example.psp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class PspApplication {

    public static void main(String[] args) {
//        System.out.println("DATASOURCE_URL = " + System.getenv("DATASOURCE_URL"));
        SpringApplication.run(PspApplication.class, args);
    }


//    @GetMapping("/health")
//    public String hello() {
//        return "Hello from PSP Service!";
//    }
}

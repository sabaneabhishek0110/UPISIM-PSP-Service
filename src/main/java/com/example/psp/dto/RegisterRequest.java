package com.example.psp.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String phone;
    private String password;
    private String bankName;

    public RegisterRequest() {
    }
    public RegisterRequest(String name, String phone, String password, String bankName) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.bankName = bankName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}

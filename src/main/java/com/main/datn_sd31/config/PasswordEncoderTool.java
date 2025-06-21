package com.main.datn_sd31.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTool {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "quyen";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Mật khẩu mã hóa: " + encodedPassword);
    }
}
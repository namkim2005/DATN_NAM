package com.main.datn_sd31.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderTool {

//    static PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "100302h@";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Mật khẩu mã hóa: " + encodedPassword);

//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//        String raw = "hung";
//        String encodedFromDB = "$2a$10$.VWthNXI3AkTGQzaLsWB7.gHb8fvMUXypIPTUTM9DFsNyJITFR0Vi"; // copy từ DB
//
//        boolean match = passwordEncoder.matches(raw, encodedFromDB);
//        System.out.println("Match: " + match); // true or false
    }

}
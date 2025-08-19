package com.main.datn_sd31.controller;

import com.main.datn_sd31.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Controller
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    // ==================== LOGOUT ENDPOINTS ====================
    
    @GetMapping("/admin/logout")
    public String adminLogout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/admin/dang-nhap?logout=true";
    }

    @GetMapping("/customer/logout")
    public String customerLogout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/san-pham/danh-sach?logout=true";
    }

    // ==================== AUTH STATUS ENDPOINT ====================
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus() {
        return ResponseEntity.ok(authenticationService.getCurrentUserInfo());
    }
} 
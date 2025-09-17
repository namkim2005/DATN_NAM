package com.main.datn_SD113.config;

import com.main.datn_SD113.entity.NhanVien;
import com.main.datn_SD113.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserControllerAdvice {
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @ModelAttribute("currentUser")
    public NhanVien getCurrentUser() {
        return authenticationService.getCurrentEmployee();
    }
}

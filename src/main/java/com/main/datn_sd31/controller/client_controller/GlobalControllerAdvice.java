package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.DanhMuc;
import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.repository.Danhmucrepository;
import com.main.datn_sd31.repository.KhachHangRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @Autowired
    private Danhmucrepository danhMucRepository;

    @ModelAttribute
    public void addKhachHangToSession(Authentication authentication, HttpSession session) {
        if (authentication != null && session.getAttribute("khachHang") == null) {
            String email = authentication.getName();
            khachHangRepository.findByEmail(email).ifPresent(khachHang -> {
                session.setAttribute("khachHang", khachHang);
            });
        }
    }
    
    @ModelAttribute("danhMucs")
    public List<DanhMuc> addDanhMucsToModel() {
        return danhMucRepository.findAll().stream()
                .filter(dm -> dm.getTrangThai() != null && dm.getTrangThai())
                .toList();
    }
}
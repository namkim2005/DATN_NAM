package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.SpYeuThich;
import com.main.datn_sd31.repository.SpYeuThichRepository;
import com.main.datn_sd31.service.impl.KhachHangServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.List;

@Controller
@ControllerAdvice
@RequiredArgsConstructor
public class LayoutController {

    private final SpYeuThichRepository spYeuThichRepository;
    private final KhachHangServiceImpl khachHangServiceImpl;

    @ModelAttribute("wishlistCount")
    public int getWishlistCount(Principal principal) {
        if (principal != null) {
            try {
                String username = principal.getName();
                KhachHang khachHang = khachHangServiceImpl.findByEmail(username);
                if (khachHang != null) {
                    List<SpYeuThich> wishlist = spYeuThichRepository.findByKhachHang_IdOrderByThoiGianThemDesc(khachHang.getId());
                    return wishlist != null ? wishlist.size() : 0;
                }
            } catch (Exception e) {
                // Log error if needed
                return 0;
            }
        }
        return 0;
    }
} 
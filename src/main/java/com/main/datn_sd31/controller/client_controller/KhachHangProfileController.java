package com.main.datn_sd31.controller.client_controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/khach-hang")
public class KhachHangProfileController {

    @GetMapping("/thong-tin")
    public String thongTinTaiKhoan(Model model) {
        model.addAttribute("activePage", "profile");
        return "client/pages/profile"; // Sẽ tạo sau
    }

    @GetMapping("/don-hang")
    public String donHangCuaToi(Model model) {
        model.addAttribute("activePage", "orders");
        return "client/pages/orders"; // Sẽ tạo sau
    }

    @GetMapping("/yeu-thich")
    public String sanPhamYeuThich(Model model) {
        model.addAttribute("activePage", "wishlist");
        return "client/pages/wishlist"; // Sẽ tạo sau
    }

    @GetMapping("/dia-chi")
    public String diaChiGiaoHang(Model model) {
        model.addAttribute("activePage", "address");
        return "client/pages/address"; // Sẽ tạo sau
    }
} 
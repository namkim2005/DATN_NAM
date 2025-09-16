package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.SpYeuThich;
import com.main.datn_sd31.entity.ChiTietSanPham;
import com.main.datn_sd31.repository.SpYeuThichRepository;
import com.main.datn_sd31.service.impl.KhachHangServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/khach-hang")
public class KhachHangProfileController {

    private final SpYeuThichRepository spYeuThichRepository;
    private final KhachHangServiceImpl khachHangServiceImpl;

    @GetMapping("/thong-tin")
    public String thongTinTaiKhoan(Model model) {
        model.addAttribute("activePage", "profile");
        return "client/pages/profile/profile";
    }

    @GetMapping("/don-hang")
    public String donHangCuaToi(Model model) {
        model.addAttribute("activePage", "orders");
        return "client/pages/profile/orders";
    }

    @GetMapping("/yeu-thich")
    public String sanPhamYeuThich(Model model, Principal principal) {
        model.addAttribute("activePage", "wishlist");
        
        if (principal != null) {
            String username = principal.getName();
            KhachHang khachHang = khachHangServiceImpl.findByEmail(username);
            
            if (khachHang != null) {
                // Lấy danh sách sản phẩm yêu thích với JOIN FETCH để load hinhAnhs
                List<SpYeuThich> wishlist = spYeuThichRepository.findByKhachHang_IdOrderByThoiGianThemDesc(khachHang.getId());
                
                // Force load hinhAnhs để tránh lazy loading
                for (SpYeuThich item : wishlist) {
                    if (item.getSanPham() != null) {
                        // Force load hinhAnhs
                        item.getSanPham().getHinhAnhs().size();
                    }
                }
                
                // Tính toán giá cho từng sản phẩm
                Map<Integer, BigDecimal> giaBanMinMap = new HashMap<>();
                Map<Integer, BigDecimal> giaBanMaxMap = new HashMap<>();
                
                for (SpYeuThich item : wishlist) {
                    if (item.getSanPham() != null && item.getSanPham().getChiTietSanPhams() != null) {
                        Optional<BigDecimal> minPrice = item.getSanPham().getChiTietSanPhams().stream()
                            .filter(ct -> ct.getGiaBan() != null)
                            .map(ChiTietSanPham::getGiaBan)
                            .min(BigDecimal::compareTo);
                        
                        Optional<BigDecimal> maxPrice = item.getSanPham().getChiTietSanPhams().stream()
                            .filter(ct -> ct.getGiaBan() != null)
                            .map(ChiTietSanPham::getGiaBan)
                            .max(BigDecimal::compareTo);
                        
                        minPrice.ifPresent(price -> giaBanMinMap.put(item.getSanPham().getId(), price));
                        maxPrice.ifPresent(price -> giaBanMaxMap.put(item.getSanPham().getId(), price));
                    }
                }
                
                model.addAttribute("wishlist", wishlist);
                model.addAttribute("khachHang", khachHang);
                model.addAttribute("giaBanMinMap", giaBanMinMap);
                model.addAttribute("giaBanMaxMap", giaBanMaxMap);
            }
        }
        
        return "client/pages/profile/wishlist";
    }

    @GetMapping("/dia-chi")
    public String diaChiGiaoHang(Model model) {
        model.addAttribute("activePage", "address");
        return "client/pages/profile/address";
    }
}

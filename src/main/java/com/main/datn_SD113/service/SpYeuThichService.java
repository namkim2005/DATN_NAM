package com.main.datn_SD113.service;

import com.main.datn_SD113.entity.KhachHang;
import com.main.datn_SD113.entity.SanPham;
import com.main.datn_SD113.entity.SpYeuThich;
import com.main.datn_SD113.repository.KhachHangRepository;
import com.main.datn_SD113.repository.SanPhamRepository;
import com.main.datn_SD113.repository.SpYeuThichRepository;
import com.main.datn_SD113.service.impl.Sanphamservice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpYeuThichService {

    private final SpYeuThichRepository repo;
    private final KhachHangService khachHangService;
    private final KhachHangRepository khachHangRepository;
    private final SanPhamRepository sanPhamRepository;
    private final Sanphamservice sanphamservice;

    // SỬA: Đổi thứ tự tham số cho đúng logic
    public boolean themVaoYeuThich(Integer khachHangId, Integer sanPhamId) {
        if (repo.existsBySanPham_IdAndKhachHang_Id(sanPhamId, khachHangId)) {
            return false; // đã có trong wishlist
        }

        SpYeuThich st = new SpYeuThich();
        st.setSanPham(sanphamservice.findbyid(sanPhamId));
        st.setKhachHang(khachHangRepository.findById(khachHangId).orElse(null));
        st.setThoiGianThem(LocalDateTime.now());
        repo.save(st);
        return true;
    }

    // SỬA: Đổi thứ tự tham số cho đúng logic
    public boolean xoaKhoiYeuThich(Integer khachHangId, Integer sanPhamId) {
        Optional<SpYeuThich> sp = repo.findBySanPham_IdAndKhachHang_Id(sanPhamId, khachHangId);
        sp.ifPresent(repo::delete);
        return sp.isPresent();
    }
}
package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.service.KhachHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KhachHangServiceImpl implements KhachHangService {

    private final KhachHangRepository khachHangRepository;

    @Override
    public KhachHang findByEmail(String email) {
        return khachHangRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với email: " + email));
    }

    @Override
    public void save(KhachHang khachHang) {
        khachHangRepository.save(khachHang);
    }

    @Override
    public void capNhatEmail(Integer id, String emailMoi) {
        KhachHang kh = khachHangRepository.find(id);
        kh.setEmail(emailMoi);
        khachHangRepository.save(kh);
    }

    @Override
    public void capNhatSoDienThoai(Integer id, String soDienThoaiMoi) {
        KhachHang kh = khachHangRepository.find(id);
        kh.setSoDienThoai(soDienThoaiMoi);
        khachHangRepository.save(kh);
    }

    @Override
    public void capNhatDiaChi(Integer id, String diaChiMoi) {
        KhachHang kh = khachHangRepository.find(id);
        kh.setDiaChi(diaChiMoi);
        khachHangRepository.save(kh);
    }
}

package com.main.datn_sd31.service;

import com.main.datn_sd31.entity.KhachHang;

public interface KhachHangService {

    KhachHang findByEmail(String email);

    void save(KhachHang khachHang);

    void capNhatEmail(Integer id, String emailMoi);

    void capNhatSoDienThoai(Integer id, String soDienThoaiMoi);

    void capNhatDiaChi(Integer id, String diaChiMoi);
}

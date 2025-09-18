package com.main.datn_SD113.service;

import com.main.datn_SD113.dto.thong_ke_dto.ThongKeSanPhamDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ThongKeService {

    BigDecimal getDoanhThu(LocalDateTime start, LocalDateTime end, Integer trangThai);

    Integer countDonHang(LocalDateTime start, LocalDateTime end, Integer trangThai);

    Integer getTongSanPham(LocalDateTime start, LocalDateTime end);

    List<ThongKeSanPhamDTO> getThongKeSanPham(LocalDateTime start, LocalDateTime end);
}

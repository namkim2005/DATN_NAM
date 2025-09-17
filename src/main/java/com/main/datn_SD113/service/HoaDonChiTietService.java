package com.main.datn_SD113.service;

import com.main.datn_SD113.dto.hoa_don_dto.HoaDonChiTietDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface HoaDonChiTietService {

    List<HoaDonChiTietDTO> getHoaDonChiTietByMaHoaDon(String maHoaDon);

    HoaDonChiTietDTO capNhatSoLuong(Integer id, Integer soLuongMoi);

}

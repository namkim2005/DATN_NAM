package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.LichSuHoaDon;
import com.main.datn_sd31.repository.HoaDonRepository;
import com.main.datn_sd31.repository.LichSuHoaDonRepository;
import com.main.datn_sd31.service.HoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HoaDonServiceIpml implements HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;

    @Override
    public List<HoaDonDTO> getAllHoaDon() {
        List<HoaDonDTO> hoaDonList = new ArrayList<>();

        for (HoaDon hoaDon : hoaDonRepository.findAll()) {
            HoaDonDTO hoaDonDTO = new HoaDonDTO();
            hoaDonDTO.setMa(hoaDon.getMa());

            if (hoaDon.getKhachHang() != null) {
                hoaDonDTO.setMaKH(hoaDon.getKhachHang().getMa());
                hoaDonDTO.setTenKH(hoaDon.getKhachHang().getTen());
            } else {
                hoaDonDTO.setMaKH(null);
                hoaDonDTO.setTenKH("Khách vãng lai");
            }

            LichSuHoaDon lichSuMoiNhat = lichSuHoaDonRepository.findTopByHoaDonOrderByIdDesc(hoaDon);
            if (lichSuMoiNhat != null) {
                hoaDonDTO.setTrangThai(lichSuMoiNhat.getTrangThai());
            } else {
                hoaDonDTO.setTrangThai(7);
            }

            hoaDonDTO.setThanhTien(hoaDon.getThanhTien());
            hoaDonList.add(hoaDonDTO);
        }

        return hoaDonList;
    }

}

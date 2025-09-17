package com.main.datn_SD113.util;

import com.main.datn_SD113.entity.PhieuGiamGia;
import com.main.datn_SD113.service.HoaDonService;
import com.main.datn_SD113.service.PhieuGiamGiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("phieu_giam_gia_utils")
@RequiredArgsConstructor
public class PhieuGiamGiaUtils {

    private final HoaDonService hoaDonService;

    private final PhieuGiamGiaService phieuGiamGiaService;

    public boolean khongChoPhepXoaPhieuGiamGia(Integer id) {
        PhieuGiamGia phieuGiamGia = phieuGiamGiaService.findById(id);
        return hoaDonService.existsByPhieuGiamGia(phieuGiamGia);
    }

}

package com.main.datn_sd31.dto.phieu_giam_gia;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhieuGiamGiaDto {

    private Integer id;
    private String ma;
    private String ten;

    //Thay bằng kieeur Integer, 1: Phan tram, 2: So tien
    private Integer loaiPhieuGiamGia;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private BigDecimal mucDo;
    private BigDecimal giamToiDa;
    private BigDecimal dieuKien;
    private Integer soLuongTon;
}
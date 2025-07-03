package com.main.datn_sd31.dto;

import com.main.datn_sd31.entity.ChiTietSanPham;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sanphamform {
    private String ma;
    private String ten;
    private String mota;
    private Integer trangthai;

    private Integer chatLieuId;
    private Integer danhMucId;
    private Integer kieuDangId;
    private Integer thuongHieuId;
    private Integer xuatXuId;
    private Integer loaiThuId;

    private List<ChiTietSanPham> chiTietList = new ArrayList<>();
}
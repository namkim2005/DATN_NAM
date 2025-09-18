package com.main.datn_SD113.dto.san_pham_DTO;

import com.main.datn_SD113.entity.ChiTietSanPham;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Sanphamform {
    private Integer id;
    private String ma;
    private String ten;
    private String mota;
    private Integer trangthai;

    private Integer chatLieuId;
    private Integer danhMucId;
    private Integer kieuDangId;
    private Integer thuongHieuId;
    private Integer xuatXuId;

    private List<ChiTietSanPham> chiTietList = new ArrayList<>();
}

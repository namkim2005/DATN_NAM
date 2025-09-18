package com.main.datn_SD113.dto.san_pham_DTO;

import com.main.datn_SD113.entity.ChiTietSanPham;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChiTietSanPhamForm {
    private List<ChiTietSanPham> chiTietList = new ArrayList<>();
}

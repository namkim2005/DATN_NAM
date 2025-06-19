package com.main.datn_sd31.dto.hoa_don_dto;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonDTO {

    private String ma;

    private String maKH;

    private String tenKH;

    private Integer trangThai;

    private BigDecimal thanhTien;

    private String ngayTao;

    public String getTrangThai1() {
        TrangThaiLichSuHoaDon tt = TrangThaiLichSuHoaDon.fromValue(this.trangThai);
        return tt != null ? tt.getMoTa() : "Không rõ";
    }

}

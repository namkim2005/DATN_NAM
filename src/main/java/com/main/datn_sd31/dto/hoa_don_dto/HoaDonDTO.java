package com.main.datn_sd31.dto.hoa_don_dto;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    public String getTrangThai() {
        TrangThaiLichSuHoaDon tt = TrangThaiLichSuHoaDon.fromValue(this.trangThai);
        return tt != null ? tt.getMoTa() : "Không rõ";
    }

}

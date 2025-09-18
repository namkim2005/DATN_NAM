package com.main.datn_SD113.service;

import com.main.datn_SD113.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_SD113.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_SD113.dto.lich_su_hoa_don_dto.KetQuaCapNhatTrangThai;
import com.main.datn_SD113.dto.lich_su_hoa_don_dto.LichSuHoaDonDTO;
import com.main.datn_SD113.entity.KhachHang;
import com.main.datn_SD113.entity.LichSuHoaDon;
import com.main.datn_SD113.entity.NhanVien;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface LichSuHoaDonService {

    List<LichSuHoaDon> getLichSuHoaDonByHoaDon(String maHoaDon);


//    TrangThaiLichSuHoaDon getTrangThaiTruocDo(String maHoaDon);

    List<TrangThaiLichSuHoaDon> getTrangThaiTiepTheoHopLe(TrangThaiLichSuHoaDon hienTai, HoaDonDTO hoaDonDTO);

    List<TrangThaiLichSuHoaDon> getTrangThaiTiepTheoHopLeKhachHang(TrangThaiLichSuHoaDon hienTai, HoaDonDTO hoaDonDTO);

    List<LichSuHoaDonDTO> getLichSuHoaDonDTOByHoaDon(String maHoaDon);

    List<LichSuHoaDonDTO> getLichSuHoaDonDTODescByMaHoaDon(String maHoaDon);

    void capNhatTrangThai(String maHoaDon, Integer trangThaiMoi, String ghiChu, NhanVien nhanVien);


    KetQuaCapNhatTrangThai xuLyCapNhatTrangThai(
            String maHoaDon,
            Integer trangThaiMoi,
//            Integer lyDoGiaoKhongThanhCong,
            String ghiChu,
            NhanVien nhanVien
    );

    void capNhatTrangThaiByKhachHang(String maHoaDon, Integer trangThaiMoi, String ghiChu, KhachHang khachHang);

    KetQuaCapNhatTrangThai xuLyCapNhatTrangThaiKhachHang(
            String maHoaDon,
            Integer trangThaiMoi,
//            Boolean quayLui,
            String ghiChu,
            KhachHang khachHang
    );

    void updateStatusAfter3Days();

}
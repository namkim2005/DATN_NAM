package com.main.datn_sd31.Enum;

public enum LyDoGiaoKhongThanhCong {
    SHIPPER_MAT_DON("Shipper làm mất đơn"),
    KHACH_KHONG_LIEN_LAC_DUOC("Khách không liên lạc được"),
    KHACH_TU_CHOI_NHAN("Khách từ chối nhận hàng"),
    DIA_CHI_KHONG_HOP_LE("Địa chỉ giao hàng không hợp lệ"),
    HANG_HOA_HU_HONG("Hàng hóa bị hư hỏng trong quá trình vận chuyển"),
    DICH_BENH_HOAC_THIEN_TAI("Do dịch bệnh hoặc thiên tai");
//    KHAC("Lý do khác");

    private final String moTa;

    LyDoGiaoKhongThanhCong(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }
}
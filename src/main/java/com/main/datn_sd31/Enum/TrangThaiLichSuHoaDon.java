package com.main.datn_sd31.Enum;

public enum TrangThaiLichSuHoaDon {
    CHO_XAC_NHAN(1, "Chờ xác nhận"),
    XAC_NHAN(2, "Xác nhận"),
    CHO_GIAO_HANG(3, "Chờ giao hàng"),
    HOAN_THANH(4, "Hoàn thành"),
    HUY(5, "Hủy"),

    HOAN_HANG(6, "Hoàn hàng"),
    DA_HOAN_TIEN(7, "Đã hoàn tiền"),
    HOAN_THANH_CONG(8, "Hoàn thành công");

    private final int value;
    private final String moTa;

    TrangThaiLichSuHoaDon(int value, String moTa) {
        this.value = value;
        this.moTa = moTa;
    }

    public int getValue() {
        return value;
    }

    public String getMoTa() {
        return moTa;
    }

    public static TrangThaiLichSuHoaDon fromValue(int value) {
        for (TrangThaiLichSuHoaDon tt : values()) {
            if (tt.value == value) {
                return tt;
            }
        }
        return null;
    }
}

package com.main.datn_sd31.Enum;

public enum TrangThaiLichSuHoaDon {
    CHO_XAC_NHAN(1, "Chờ xác nhận"),
    XAC_NHAN(2, "Xác nhận"),
    CHO_GIAO_HANG(3, "Chờ giao hàng"),
    DA_GIAO(4, "Đã giao"),
    HOAN_THANH(5, "Hoàn thành"),
    HUY(6, "Hủy");

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

package com.main.datn_sd31.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "hoa_don_chi_tiet")
public class HoaDonChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nguoi_tao")
    private Integer nguoiTao;

    @Column(name = "nguoi_sua")
    private Integer nguoiSua;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
    private LocalDate ngayTao;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_sua")
    private LocalDate ngaySua;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Boolean trangThai = false;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hoa_don_id", nullable = false)
    private HoaDon hoaDon;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chi_tiet_san_pham_id", nullable = false)
    private ChiTietSanPham chiTietSanPham;

    @NotNull
    @Column(name = "gia_sau_giam", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaSauGiam;

    @NotNull
    @Column(name = "gia_giam", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaGiam;

    @NotNull
    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

}
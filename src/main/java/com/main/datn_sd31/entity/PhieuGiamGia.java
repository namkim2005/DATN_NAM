package com.main.datn_sd31.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "ma", nullable = false, length = 50)
    private String ma;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "ten", nullable = false, length = 100)
    private String ten;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua;

    @Column(name = "nguoi_tao")
    private Integer nguoiTao;

    @Column(name = "nguoi_sua")
    private Integer nguoiSua;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Boolean trangThai = false;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "loai_phieu_giam_gia", nullable = false, length = 50)
    private String loaiPhieuGiamGia;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @NotNull
    @Column(name = "muc_do", nullable = false, precision = 18, scale = 2)
    private BigDecimal mucDo;

    @NotNull
    @Column(name = "giam_toi_da", nullable = false, precision = 18, scale = 2)
    private BigDecimal giamToiDa;

    @NotNull
    @Column(name = "dieu_kien", nullable = false, precision = 18, scale = 2)
    private BigDecimal dieuKien;

    @NotNull
    @Column(name = "so_luong_ton", nullable = false)
    private Integer soLuongTon;

    @OneToMany(mappedBy = "phieuGiamGia")
    private Set<HoaDon> hoaDons = new LinkedHashSet<>();

}
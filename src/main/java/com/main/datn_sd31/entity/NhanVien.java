package com.main.datn_sd31.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "nhan_vien")
public class NhanVien {
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

    @NotNull
    @Column(name = "ngay_sinh", nullable = false)
    private LocalDateTime ngaySinh;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @Column(name = "so_dien_thoai", nullable = false, length = 20)
    private String soDienThoai;

    @NotNull
    @Column(name = "ngay_tham_gia", nullable = false)
    private LocalDateTime ngayThamGia;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @Column(name = "chung_minh_thu", nullable = false, length = 20)
    private String chungMinhThu;

    @NotNull
    @Column(name = "gioi_tinh", nullable = false)
    private Boolean gioiTinh = false;

    @NotNull
    @Nationalized
    @Lob
    @Column(name = "anh", nullable = false)
    private String anh;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "mat_khau", nullable = false, length = 100)
    private String matKhau;

    @Column(name = "nguoi_tao")
    private Integer nguoiTao;

    @Column(name = "nguoi_sua")
    private Integer nguoiSua;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "chuc_vu", nullable = false, length = 50)
    private String chucVu;

    @NotNull
    @Column(name = "trang_thai", nullable = false)
    private Boolean trangThai = false;

    @OneToMany(mappedBy = "nhanVien")
    private Set<HoaDon> hoaDons = new LinkedHashSet<>();

}
package com.main.datn_sd31.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "nhan_vien")
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @Nationalized
    @Column(name = "ma", length = 50)
    private String ma;

    @Size(max = 100)
    @Nationalized
    @Column(name = "ten", length = 100)
    private String ten;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Size(max = 20)
    @Nationalized
    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "ngay_tham_gia")
    private LocalDate ngayThamGia;

    @Size(max = 20)
    @Nationalized
    @Column(name = "chung_minh_thu", length = 20)
    private String chungMinhThu;

    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;

    @Nationalized
    @Lob
    @Column(name = "anh")
    private String anh;

    @Size(max = 100)
    @Nationalized
    @Column(name = "email", length = 100)
    private String email;

    @Size(max = 100)
    @Nationalized
    @Column(name = "mat_khau", length = 100)
    private String matKhau;

    @Column(name = "nguoi_tao")
    private Integer nguoiTao;

    @Column(name = "nguoi_sua")
    private Integer nguoiSua;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_sua")
    private LocalDate ngaySua;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
    private LocalDate ngayTao;

    @Size(max = 50)
    @Nationalized
    @Column(name = "chuc_vu", length = 50)
    private String chucVu;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @OneToMany(mappedBy = "nguoiTao")
    @ToString.Exclude
    private Set<DotGiamGia> dotGiamGias = new LinkedHashSet<>();

    @OneToMany(mappedBy = "nhanVien")
    @ToString.Exclude
    private Set<HoaDon> hoaDons = new LinkedHashSet<>();

    @OneToMany(mappedBy = "nhanVien")
    @ToString.Exclude
    private Set<PhieuGiamGia> phieuGiamGias = new LinkedHashSet<>();

    @OneToMany(mappedBy = "nguoiTao")
    @ToString.Exclude
    private Set<SanPham> sanPhams = new LinkedHashSet<>();

}
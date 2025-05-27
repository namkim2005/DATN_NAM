package com.main.datn_sd31.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "chi_tiet_dot_giam_gia")
public class ChiTietDotGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chi_tiet_sp_id")
    @ToString.Exclude
    private ChiTietSanPham chiTietSp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dot_giam_gia_id")
    @ToString.Exclude
    private DotGiamGia dotGiamGia;

    @Column(name = "gia_tri_dot_giam_gia", precision = 5, scale = 2)
    private BigDecimal giaTriDotGiamGia;

}
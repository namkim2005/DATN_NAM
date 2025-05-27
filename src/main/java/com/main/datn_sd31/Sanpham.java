package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "san_pham")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Sanpham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "ma")
    private String ma;

    @Column(name = "ten")
    private String ten;

    @Column(name = "mo_ta")
    private String mota;

    @Column(name = "nguoi_tao")
    private Integer nguoitao;

    @Column(name = "nguoi_sua")
    private Integer nguoisua;

    @Column(name = "ngay_tao")
    private String ngaytao;

    @Column(name = "ngay_sua")
    private String ngaysua;

    @Column(name = "trang_thai")
    private Integer trangthai;

    @ManyToOne
    @JoinColumn(name = "chat_lieu_id",referencedColumnName = "id")
    private Chatlieu chatlieu;

    @ManyToOne
    @JoinColumn(name = "danh_muc_id",referencedColumnName = "id")
    private Danhmuc danhmuc;

    @ManyToOne
    @JoinColumn(name = "kieu_dang_id",referencedColumnName = "id")
    private Kieudang kieudang;

    @ManyToOne
    @JoinColumn(name = "thuong_hieu_id",referencedColumnName = "id")
    private Thuonghieu thuonghieu;

    @ManyToOne
    @JoinColumn(name = "xuat_xu_id",referencedColumnName = "id")
    private Xuatxu xuatxu;
}

package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "xuat_xu")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Xuatxu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "ma")
    private String ma;

    @Column(name = "ten")
    private String ten;

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
    
}

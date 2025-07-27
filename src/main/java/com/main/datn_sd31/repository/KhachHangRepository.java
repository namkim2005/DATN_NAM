package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    @Query("select k from KhachHang k where k.id = :id")
    KhachHang find(@Param("id") Integer id);

    Optional<KhachHang> findByEmail(String email);
    List<KhachHang> findByMa(String ma);
    List<KhachHang> findBySoDienThoaiContaining(String sdt);

    // thêm phương thức search đơn giản giống NhanVien
    @Query("select k from KhachHang k " +
            "where k.ten like %:search% " +
            "   or k.soDienThoai like %:search% " +
            "   or k.email like %:search%")
    List<KhachHang> search(@Param("search") String search);
}
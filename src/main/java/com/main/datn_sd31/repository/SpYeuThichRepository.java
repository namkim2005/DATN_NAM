package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.SpYeuThich;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpYeuThichRepository extends JpaRepository<SpYeuThich, Integer> {
	boolean existsBySanPham_IdAndKhachHang_Id(Integer sanPhamId, Integer khachHangId);
	Optional<SpYeuThich> findBySanPham_IdAndKhachHang_Id(Integer sanPhamId, Integer khachHangId);
} 
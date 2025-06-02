package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
}

package com.main.datn_sd31.repository.dotGiamGia;

import com.main.datn_sd31.entity.DotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia,Integer> {
}
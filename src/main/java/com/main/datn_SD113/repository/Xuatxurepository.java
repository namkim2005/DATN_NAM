package com.main.datn_SD113.repository;

import com.main.datn_SD113.entity.XuatXu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Xuatxurepository extends JpaRepository<XuatXu,Integer> {
    XuatXu findTopByOrderByMaDesc();
}

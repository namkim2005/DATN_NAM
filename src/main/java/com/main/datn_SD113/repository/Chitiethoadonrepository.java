package com.main.datn_SD113.repository;

import com.main.datn_SD113.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Chitiethoadonrepository extends JpaRepository<HoaDonChiTiet,Integer> {

}

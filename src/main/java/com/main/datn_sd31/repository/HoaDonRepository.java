package com.main.datn_sd31.repository;

import com.main.datn_sd31.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    Page<HoaDon> findByNgayTaoBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("""
        SELECT hd FROM HoaDon hd 
        WHERE LOWER(hd.ma) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(hd.tenNguoiNhan) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR hd.soDienThoai LIKE CONCAT('%', :keyword, '%')
    """)
    Page<HoaDon> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
    SELECT hd FROM HoaDon hd 
    WHERE LOWER(hd.ma) LIKE LOWER(CONCAT('%', :ma, '%'))
""")
    List<HoaDon> findByMaContainingIgnoreCase(@Param("ma") String ma);
}

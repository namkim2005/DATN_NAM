package com.main.datn_SD113.repository;

import com.main.datn_SD113.entity.DotGiamGia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface Dotgiamgiarepository extends JpaRepository<DotGiamGia,Integer>, JpaSpecificationExecutor<DotGiamGia> {
    // Kiểm tra mã đợt giảm giá đã tồn tại hay chưa
    boolean existsByMa(String ma);

    boolean existsByMaAndIdNot(@Size(max = 50) @NotNull String ma, Integer id);
}

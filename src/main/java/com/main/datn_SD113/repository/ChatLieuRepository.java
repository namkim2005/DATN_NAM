package com.main.datn_SD113.repository;

import com.main.datn_SD113.entity.ChatLieu;
import com.main.datn_SD113.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLieuRepository extends JpaRepository<ChatLieu,Integer> {
    ChatLieu findTopByOrderByMaDesc();

}

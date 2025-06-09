package com.main.datn_sd31.service;

import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.entity.HoaDon;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface HoaDonService {

    List<HoaDonDTO> getAllHoaDon();

}

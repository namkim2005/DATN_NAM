package com.main.datn_sd31.component;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.LichSuHoaDon;
import com.main.datn_sd31.repository.HoaDonRepository;
import com.main.datn_sd31.repository.LichSuHoaDonRepository;
import com.main.datn_sd31.service.HoaDonService;
import com.main.datn_sd31.service.LichSuHoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TuDongCapNhatTrangThaiHoaDon {

    private final LichSuHoaDonService lichSuHoaDonService;

    private final LichSuHoaDonRepository lichSuHoaDonRepository;

//    private final HoaDonService hoaDonService;

//    private final HoaDonRepository hoaDonRepository;

    @Scheduled(cron = "0 0 2 * * ?") // chạy lúc 2 giờ sáng mỗi ngày
//    @Transactional
    public void updateStatusAfter7Days() {
        lichSuHoaDonService.updateStatusAfter7Days();
    }

}

package com.main.datn_sd31.service.impl;

import com.main.datn_sd31.dto.Pagination;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.entity.HoaDon;
import com.main.datn_sd31.entity.LichSuHoaDon;
import com.main.datn_sd31.repository.HoaDonRepository;
import com.main.datn_sd31.repository.LichSuHoaDonRepository;
import com.main.datn_sd31.service.HoaDonService;
import com.main.datn_sd31.util.DateTimeUtils;
import com.main.datn_sd31.util.SearchUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoaDonServiceIpml implements HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;

    private HoaDonDTO mapToDTO(HoaDon hoaDon) {
        HoaDonDTO dto = new HoaDonDTO();
        dto.setMa(hoaDon.getMa());

        if (hoaDon.getKhachHang() != null) {
            dto.setMaKH(hoaDon.getKhachHang().getMa());
            dto.setTenKH(hoaDon.getKhachHang().getTen());
        } else {
            dto.setMaKH(null);
            dto.setTenKH("Khách vãng lai");
        }

        // Lấy lịch sử trạng thái mới nhất
        LichSuHoaDon lichSuMoiNhat = lichSuHoaDonRepository.findTopByHoaDonOrderByIdDesc(hoaDon);
        if (lichSuMoiNhat != null) {
            dto.setTrangThai(lichSuMoiNhat.getTrangThai());
        } else {
            dto.setTrangThai(9);
        }

        dto.setThanhTien(hoaDon.getThanhTien());
        dto.setNgayTao(DateTimeUtils.format(hoaDon.getNgayTao()));

        return dto;
    }

    @Override
    public Pagination<HoaDonDTO> getAll(Integer pageNo, Integer pageSize, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        Page<HoaDon> pageData = hoaDonRepository.findByNgayTaoBetween(start, end, pageable);

        // Chuyển Page<HoaDon> → Page<HoaDonDTO>
        Page<HoaDonDTO> pageDTO = pageData.map(this::mapToDTO);

        return new Pagination<>(pageDTO);
    }

    @Override
    public List<HoaDonDTO> getAllHoaDon() {
        List<HoaDonDTO> hoaDonList = new ArrayList<>();

        for (HoaDon hoaDon : hoaDonRepository.findAll()) {
            HoaDonDTO hoaDonDTO = new HoaDonDTO();
            hoaDonDTO.setMa(hoaDon.getMa());

            if (hoaDon.getKhachHang() != null) {
                hoaDonDTO.setMaKH(hoaDon.getKhachHang().getMa());
                hoaDonDTO.setTenKH(hoaDon.getKhachHang().getTen());
            } else {
                hoaDonDTO.setMaKH(null);
                hoaDonDTO.setTenKH("Khách vãng lai");
            }

            LichSuHoaDon lichSuMoiNhat = lichSuHoaDonRepository.findTopByHoaDonOrderByIdDesc(hoaDon);
            if (lichSuMoiNhat != null) {
                hoaDonDTO.setTrangThai(lichSuMoiNhat.getTrangThai());
            } else {
                hoaDonDTO.setTrangThai(9);
            }

            hoaDonDTO.setThanhTien(hoaDon.getThanhTien());
            hoaDonList.add(hoaDonDTO);
        }

        return hoaDonList;
    }

    @Override
    public Map<String, Long> getTrangThaiCount() {
        return getAllHoaDon().stream()
                .collect(Collectors.groupingBy(HoaDonDTO::getTrangThai1, Collectors.counting()));
    }

    @Override
    public Pagination<HoaDonDTO> getAllHoaDonByStatus(Integer status, int pageNo, int pageSize) {
        List<HoaDonDTO> all = getAll(
                0,
                Integer.MAX_VALUE,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1)
        ).getContent();
        List<HoaDonDTO> filtered = all.stream()
                .filter(hd -> Objects.equals(hd.getTrangThai(), status))
                .toList();

        int total = filtered.size();
        int fromIndex = Math.min(pageNo * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<HoaDonDTO> pageContent = filtered.subList(fromIndex, toIndex);

        Pagination<HoaDonDTO> result = new Pagination<>();
        result.setContent(pageContent);
        result.setCurrentPage(pageNo);
        result.setPageSize(pageSize);
        result.setTotalElements(total);
        result.setTotalPages((int) Math.ceil((double) total / pageSize));
        result.setLast(toIndex == total);

        return result;
    }

    @Override
    public Pagination<HoaDonDTO> searchByKeyword(String keyword, int pageNo, int pageSize) {
        keyword = SearchUtils.normalizeKeyword(keyword);
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("ngayTao").descending());
        Page<HoaDon> hoaDonPage = hoaDonRepository.searchByKeyword(keyword, pageable);

        List<HoaDonDTO> content = hoaDonPage.getContent()
                .stream()
                .map(this::mapToDTO)
                .toList();

        return new Pagination<>(hoaDonPage.map(this::mapToDTO));
    }

    @Override
    public HoaDonDTO getHoaDonByMa(String ma) {
        List<HoaDon> hoaDon = hoaDonRepository.findByMaContainingIgnoreCase(ma);
        return mapToDTO(hoaDon.isEmpty() ? null : hoaDon.get(0));
    }


}

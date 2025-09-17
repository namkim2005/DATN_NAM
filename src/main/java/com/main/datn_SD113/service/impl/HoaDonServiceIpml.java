package com.main.datn_SD113.service.impl;

import com.main.datn_SD113.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_SD113.dto.Pagination;
import com.main.datn_SD113.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_SD113.entity.HoaDon;
import com.main.datn_SD113.entity.KhachHang;
import com.main.datn_SD113.entity.LichSuHoaDon;
import com.main.datn_SD113.entity.PhieuGiamGia;
import com.main.datn_SD113.repository.HoaDonRepository;
import com.main.datn_SD113.repository.LichSuHoaDonRepository;
import com.main.datn_SD113.service.HoaDonService;
import com.main.datn_SD113.util.DateTimeUtils;
import com.main.datn_SD113.util.SearchUtils;
import com.main.datn_SD113.util.ThymleafHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

        dto.setDiaChi(hoaDon.getDiaChi());
        dto.setEmail(hoaDon.getEmail());
        dto.setSoDienThoai(hoaDon.getSoDienThoai());
        dto.setTenNguoiNhan(hoaDon.getTenNguoiNhan());

        if (hoaDon.getNhanVien() != null) {
            dto.setMaNV(hoaDon.getNhanVien().getMa());
            dto.setTenNhanVien(hoaDon.getNhanVien().getTen());
        } else {
            dto.setMaNV(null);
            dto.setTenNhanVien("Auto");
        }

        // Lấy lịch sử trạng thái mới nhất
        LichSuHoaDon lichSuMoiNhat = lichSuHoaDonRepository.findTopByHoaDonOrderByIdDesc(hoaDon);
        if (lichSuMoiNhat != null) {
            dto.setTrangThaiLichSuHoaDon(lichSuMoiNhat.getTrangThai());
            if (lichSuMoiNhat.getNgayTao() != null) {
                dto.setCapNhatLanCuoi(DateTimeUtils.format(lichSuMoiNhat.getNgayTao()));
            } else {
                dto.setCapNhatLanCuoi(DateTimeUtils.format(hoaDon.getNgayTao()));
            }
//            dto.setLyDoGiaoKhongThanhCongEnum(lichSuMoiNhat.getLyDoGiaoKhongThanhCong());
        } else {
            dto.setTrangThaiLichSuHoaDon(10);
            dto.setCapNhatLanCuoi(DateTimeUtils.format(hoaDon.getNgayTao()));
            dto.setLyDoGiaoKhongThanhCongEnum(null);
        }

        String phuongThucDb = hoaDon.getPhuongThuc();
        String phuongThucVn = switch (phuongThucDb) {
            case "chuyen_khoan" -> "Chuyển khoản";
            case "tien_mat" -> "Tiền mặt";
            default -> " "; // fallback
        };

        dto.setPhuongThuc(phuongThucVn);

        dto.setLoaihoadon(hoaDon.getLoaihoadon());
        dto.setGiaGoc(hoaDon.getGiaGoc());

        if (hoaDon.getPhieuGiamGia() != null && hoaDon.getPhieuGiamGia().getMa() != null) {
            dto.setMaGiamGia(hoaDon.getPhieuGiamGia().getMa());
            dto.setGiamGia((ThymleafHelper.formatCurrency(hoaDon.getPhieuGiamGia().getMucDo())) +
                    (hoaDon.getPhieuGiamGia().getLoaiPhieuGiamGia() == 1 ? " %" : " đ"));
            dto.setGiaGiamGia(hoaDon.getGiaGiamGia());
        } else {
            dto.setMaGiamGia(null);
            dto.setGiamGia(null);
            dto.setGiaGiamGia(BigDecimal.valueOf(0));
        }

        dto.setThanhTien(hoaDon.getThanhTien());
        dto.setPhiVanChuyen(hoaDon.getPhiVanChuyen());
        dto.setNgayTao(DateTimeUtils.format(hoaDon.getNgayTao()));
        dto.setTrangThaiHoaDonString(hoaDon.getTrangThaiMoTa());
        dto.setTrangThaiHoaDonInteger(hoaDon.getTrangThai());

        if (hoaDon.getGhiChu() != null && !hoaDon.getGhiChu().isEmpty()) {
            dto.setGhiChu(hoaDon.getGhiChu());
        } else {
            dto.setGhiChu("");
        }



        return dto;
    }

    @Override
    public Pagination<HoaDonDTO> getAll(Integer pageNo, Integer pageSize, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        Page<HoaDon> pageData = hoaDonRepository.getHoaDon(start, end, pageable);

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
                hoaDonDTO.setTrangThaiLichSuHoaDon(lichSuMoiNhat.getTrangThai());
            } else {
                hoaDonDTO.setTrangThaiLichSuHoaDon(10);
            }

            hoaDonDTO.setThanhTien(hoaDon.getThanhTien());
            hoaDonList.add(hoaDonDTO);
        }

        return hoaDonList;
    }

    @Override
    public List<HoaDonDTO> getAllHoaDonKhachHang(KhachHang khachHang) {
        List<HoaDonDTO> hoaDonList = new ArrayList<>();

        for (HoaDon hoaDon : hoaDonRepository.findAllByKhachHang(khachHang)) {
            HoaDonDTO hoaDonDTO = new HoaDonDTO();
            hoaDonDTO.setMa(hoaDon.getMa());
            hoaDonDTO.setNgayTao(hoaDon.getNgayTao().toString());

            if (hoaDon.getKhachHang() != null) {
                hoaDonDTO.setMaKH(hoaDon.getKhachHang().getMa());
                hoaDonDTO.setTenKH(hoaDon.getKhachHang().getTen());
            } else {
                hoaDonDTO.setMaKH(null);
                hoaDonDTO.setTenKH("Khách vãng lai");
            }

            LichSuHoaDon lichSuMoiNhat = lichSuHoaDonRepository.findTopByHoaDonOrderByIdDesc(hoaDon);
            if (lichSuMoiNhat != null) {
                hoaDonDTO.setTrangThaiLichSuHoaDon(lichSuMoiNhat.getTrangThai());
            } else {
                hoaDonDTO.setTrangThaiLichSuHoaDon(11);
            }

            hoaDonDTO.setThanhTien(hoaDon.getThanhTien());
            hoaDonList.add(hoaDonDTO);
        }

        return hoaDonList;
    }

    @Override
    public Map<String, Long> getTrangThaiCount(List<HoaDonDTO> list) {
        return list.stream()
                .collect(Collectors.groupingBy(HoaDonDTO::getTrangThaiLichSuHoaDonMoTa, Collectors.counting()));
    }

    @Override
    public Pagination<HoaDonDTO> getAllHoaDonByStatus(TrangThaiLichSuHoaDon status, int pageNo, int pageSize) {
        List<HoaDonDTO> all = getAll(
                0,
                Integer.MAX_VALUE,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1)
        ).getContent();

        List<HoaDonDTO> filtered = all.stream()
                .filter(hd -> Objects.equals(hd.getTrangThaiLichSuHoaDon(), status))
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

//    @Override
//    public Pagination<HoaDonDTO> searchByKeyword(String keyword, String loaiHoaDon, int pageNo, int pageSize) {
//        keyword = SearchUtils.normalizeKeyword(keyword);
//        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("ngayTao").descending());
//        Page<HoaDon> hoaDonPage = hoaDonRepository.searchByKeyword(keyword, loaiHoaDon, pageable);
//
//        List<HoaDonDTO> content = hoaDonPage.getContent()
//                .stream()
//                .map(this::mapToDTO)
//                .toList();
//
//        return new Pagination<>(hoaDonPage.map(this::mapToDTO));
//    }

    @Override
    public HoaDonDTO getHoaDonByMa(String ma) {
        List<HoaDon> hoaDon = hoaDonRepository.findByMaContainingIgnoreCase(ma);
        return mapToDTO(hoaDon.get(0));
    }

    @Override
    public void capNhatGhiChuHoaDon(String ma, String ghiChu) {
        HoaDon hoaDon = hoaDonRepository.getHoaDonByMa(ma);
        hoaDon.setGhiChu(ghiChu);
        hoaDonRepository.save(hoaDon);
    }

    @Override
    public Pagination<HoaDonDTO> getAllDonHang(Integer pageNo, Integer pageSize, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        Page<HoaDon> pageData = hoaDonRepository.getDonHang(start, end, pageable);

        // Chuyển Page<HoaDon> → Page<HoaDonDTO>
        Page<HoaDonDTO> pageDTO = pageData.map(this::mapToDTO);

        return new Pagination<>(pageDTO);
    }

    @Override
    public Pagination<HoaDonDTO> getAllDonHangByStatus(TrangThaiLichSuHoaDon status, int pageNo, int pageSize) {
        List<HoaDonDTO> all = getAllDonHang(
                0,
                Integer.MAX_VALUE,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1)
        ).getContent();

        List<HoaDonDTO> filtered = all.stream()
                .filter(hd -> Objects.equals(hd.getTrangThaiLichSuHoaDon(), status))
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
    public Pagination<HoaDonDTO> getAllDonHangKhachHang(KhachHang khachHang, Integer pageNo, Integer pageSize, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

        Page<HoaDon> pageData = hoaDonRepository.getDonHangByKhachHang(start, end, khachHang.getId(), pageable);

        // Chuyển Page<HoaDon> → Page<HoaDonDTO>
        Page<HoaDonDTO> pageDTO = pageData.map(this::mapToDTO);

        return new Pagination<>(pageDTO);
    }

    @Override
    public Pagination<HoaDonDTO> getAllHoaDonKhachHangByStatus(KhachHang khachHang, TrangThaiLichSuHoaDon status, int pageNo, int pageSize) {
        List<HoaDonDTO> all = getAllDonHangKhachHang(
                khachHang,
                0,
                Integer.MAX_VALUE,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1)
        ).getContent();

        List<HoaDonDTO> filtered = all.stream()
                .filter(hd -> Objects.equals(hd.getTrangThaiLichSuHoaDon(), status))
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
    public boolean existsByPhieuGiamGia(PhieuGiamGia phieuGiamGia) {
        return hoaDonRepository.existsByPhieuGiamGia(phieuGiamGia);
    }

    @Override
    public Pagination<HoaDonDTO> searchByLoaiHoaDon(String loaiHoaDon, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("ngayTao").descending());
        Page<HoaDon> hoaDonPage = hoaDonRepository.searchHoaDonByLoai(loaiHoaDon, pageable);

        List<HoaDonDTO> content = hoaDonPage.getContent()
                .stream()
                .map(this::mapToDTO)
                .toList();

        return new Pagination<>(hoaDonPage.map(this::mapToDTO));
    }

    @Override
    public Pagination<HoaDonDTO> searchByLoaiDonHang(String loaiHoaDon, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("ngayTao").descending());
        Page<HoaDon> hoaDonPage = hoaDonRepository.searchDonHangByLoai(loaiHoaDon, pageable);

        List<HoaDonDTO> content = hoaDonPage.getContent()
                .stream()
                .map(this::mapToDTO)
                .toList();

        return new Pagination<>(hoaDonPage.map(this::mapToDTO));
    }

    //    @Override
    public Pagination<HoaDonDTO> getAllHoaDonByStatusAndLoai(TrangThaiLichSuHoaDon trangThai, String loaiHoaDon, int page, int size) {
        List<HoaDonDTO> all = getAll(
                0,
                Integer.MAX_VALUE,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1)
        ).getContent();

        List<HoaDonDTO> filtered = all.stream()
                .filter(hd -> Objects.equals(hd.getTrangThaiLichSuHoaDon(), trangThai))
                .filter(hd -> Objects.equals(hd.getLoaihoadon(), loaiHoaDon))
                .toList();

        int total = filtered.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        List<HoaDonDTO> pageContent = filtered.subList(fromIndex, toIndex);

        Pagination<HoaDonDTO> result = new Pagination<>();
        result.setContent(pageContent);
        result.setCurrentPage(page);
        result.setPageSize(size);
        result.setTotalElements(total);
        result.setTotalPages((int) Math.ceil((double) total / size));
        result.setLast(toIndex == total);

        return result;
    }

//    @Override
    public Pagination<HoaDonDTO> getAllByLoaiAndDate(String loaiHoaDon, LocalDate startDate, LocalDate endDate, int page, int size) {
        List<HoaDonDTO> all = getAll(
                0,
                Integer.MAX_VALUE,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2030, 1, 1)
        ).getContent();

        List<HoaDonDTO> filtered = all.stream()
//                .filter(hd -> Objects.equals(hd.getTrangThaiLichSuHoaDon(), trangThai))
                .filter(hd -> Objects.equals(hd.getLoaihoadon(), loaiHoaDon))
                .toList();

        int total = filtered.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        List<HoaDonDTO> pageContent = filtered.subList(fromIndex, toIndex);

        Pagination<HoaDonDTO> result = new Pagination<>();
        result.setContent(pageContent);
        result.setCurrentPage(page);
        result.setPageSize(size);
        result.setTotalElements(total);
        result.setTotalPages((int) Math.ceil((double) total / size));
        result.setLast(toIndex == total);

        return result;
    }


}
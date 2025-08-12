package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.*;
import com.main.datn_sd31.entity.DotGiamGia;
import com.main.datn_sd31.repository.*;
import com.main.datn_sd31.service.impl.DanhGiaService;
import com.main.datn_sd31.service.impl.Sanphamservice;
import com.main.datn_sd31.util.GetKhachHang;
import com.main.datn_sd31.util.ColorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/san-pham")
public class ListSanPhamController {
    private final Sanphamservice sanPhamService;
    private final NhanVienRepository nhanvienRepo;
    private final ChatLieuRepository chatLieuRepo;
    private final Danhmucrepository danhMucRepo;
    private final Thuonghieurepository thuongHieuRepo;
    private final Xuatxurepository xuatXuRepo;
    private final Kieudangrepository kieuDangRepo;
    private final Sizerepository sizerepository;
    private final Mausacrepository mausacrepository;

    private final Chitietsanphamrepository chitietsanphamRepo;
    private final Hinhanhrepository hinhanhrepository;
    private final Loaithurepository loaithurepository;
    private final Dotgiamgiarepository dotgiamgiarepository;
    private final SanPhamRepository sanPhamRepository;
    private final DanhGiaService danhGiaService;

    private final GetKhachHang getKhachHang;

    @GetMapping("/danh-sach")
    public String hienThiDanhSachSanPham(
            @RequestParam(value="q", required=false) String q,
            @RequestParam(value="danhMucId", required=false) Integer danhMucId,
                    @RequestParam(value="priceRange", required=false) Integer priceRange,
        @RequestParam(value="loaiThuId", required=false) Integer loaiThuId,
        @RequestParam(value="sizeId", required=false) Integer sizeId,
        @RequestParam(value="mauSacId", required=false) Integer mauSacId,
        @RequestParam(value="kieuDangId", required=false) Integer kieuDangId,
        @RequestParam(value="thuongHieuId", required=false) Integer thuongHieuId,
        @RequestParam(value="xuatXuId", required=false) Integer xuatXuId,

            Model model
    ) {
        if (getKhachHang.getCurrentKhachHang() != null) {
            Integer currentId = getKhachHang.getCurrentKhachHang().getId();
            model.addAttribute("idKhachHang", currentId);
        }
        // Sử dụng method search mới
        List<SanPham> danhSachSanPham = sanPhamService.searchAdvanced(
            q, danhMucId, loaiThuId, sizeId, mauSacId, kieuDangId, thuongHieuId, xuatXuId, priceRange
        );
        model.addAttribute("danhSachSanPham", danhSachSanPham);

        // panel filter data
        model.addAttribute("danhMucs", danhMucRepo.findAll());
        model.addAttribute("loaiThus", loaithurepository.findAll());      // <-- thêm dòng này
        model.addAttribute("sizes", sizerepository.findAll());
        
        // Process colors with fallback
        List<MauSac> mauSacs = mausacrepository.findAll();
        List<MauSac> processedMauSacs = processColorsWithFallback(mauSacs);
        model.addAttribute("mauSacs", processedMauSacs);
        
        model.addAttribute("thuongHieus", thuongHieuRepo.findAll());
        model.addAttribute("kieuDangs", kieuDangRepo.findAll());      // <-- thêm dòng này
        model.addAttribute("xuatXus", xuatXuRepo.findAll());      // <-- thêm dòng này



        // Tính toán giá cho mỗi sản phẩm
        List<ChiTietSanPham> chiTiets = chitietsanphamRepo.findAll();
        
        // Map giá gốc và giá bán cho mỗi sản phẩm
        Map<Integer, BigDecimal> giaGocMap = chiTiets.stream()
            .collect(Collectors.groupingBy(
                ct -> ct.getSanPham().getId(),
                Collectors.collectingAndThen(
                    Collectors.maxBy(Comparator.comparing(ChiTietSanPham::getGiaGoc)),
                    opt -> opt.map(ChiTietSanPham::getGiaGoc).orElse(BigDecimal.ZERO)
                )
            ));

        Map<Integer, BigDecimal> giaBanMap = chiTiets.stream()
            .collect(Collectors.groupingBy(
                ct -> ct.getSanPham().getId(),
                Collectors.collectingAndThen(
                    Collectors.minBy(Comparator.comparing(ChiTietSanPham::getGiaBan)),
                    opt -> opt.map(ChiTietSanPham::getGiaBan).orElse(BigDecimal.ZERO)
                )
            ));

        // Set thuộc tính dotGiamGia cho mỗi sản phẩm
        Map<Integer, DotGiamGia> dotGiamGiaMap = chiTiets.stream()
            .filter(ct -> ct.getDotGiamGia() != null)
            .collect(Collectors.groupingBy(
                ct -> ct.getSanPham().getId(),
                Collectors.collectingAndThen(
                    Collectors.maxBy(Comparator.comparing(
                        ct -> ct.getDotGiamGia().getGiaTriDotGiamGia())),
                    opt -> opt.map(ChiTietSanPham::getDotGiamGia).orElse(null)
                )
            ));

        // Set dotGiamGia cho mỗi sản phẩm
        danhSachSanPham.forEach(sanPham -> {
            sanPham.setDotGiamGia(dotGiamGiaMap.get(sanPham.getId()));
        });

        model.addAttribute("giaGocMap", giaGocMap);
        model.addAttribute("giaBanMap", giaBanMap);

        // Thêm các tham số filter vào model để giữ trạng thái
        model.addAttribute("q", q);
        model.addAttribute("danhMucId", danhMucId);
        model.addAttribute("priceRange", priceRange);
        model.addAttribute("loaiThuId", loaiThuId);
        model.addAttribute("sizeId", sizeId);
        model.addAttribute("mauSacId", mauSacId);
        model.addAttribute("kieuDangId", kieuDangId);
        model.addAttribute("thuongHieuId", thuongHieuId);
        model.addAttribute("xuatXuId", xuatXuId);

        return "client/pages/product";
    }


    @GetMapping("/chi-tiet/{id}")
    public String xemChiTietSanPham(
            @PathVariable("id") Integer id,
            @RequestParam(value="page", defaultValue="0") int page,
            @RequestParam(value="star", required=false) Integer star,
            Model model
    ) {
        // --- 1. LOAD SẢN PHẨM, HÌNH, SIZE, MÀU ---
        SanPham sanPham = sanPhamService.findbyid(id);
        model.addAttribute("sanPham", sanPham);
        model.addAttribute("dsSanPham", sanPhamService.getAll());
        model.addAttribute("hinhanh", hinhanhrepository.findByhinhanhid(id));

        List<ChiTietSanPham> chiTiets = chitietsanphamRepo.findBySanPhamId(id);

        // Màu sắc duy nhất
        List<MauSac> dsMauSac = chiTiets.stream()
                .map(ChiTietSanPham::getMauSac)
                .filter(ms -> ms != null && ms.getId() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(MauSac::getId, Function.identity(), (a,b)->a),
                        m -> new ArrayList<>(m.values())
                ));
        model.addAttribute("dsMauSac", dsMauSac);
        model.addAttribute("mauSacCount", dsMauSac.size());

        // Size duy nhất
        List<Size> dsSize = chiTiets.stream()
                .map(ChiTietSanPham::getSize)
                .filter(sz -> sz != null && sz.getId() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Size::getId, Function.identity(), (a,b)->a),
                        m -> new ArrayList<>(m.values())
                ));
        model.addAttribute("dsSize", dsSize);
        model.addAttribute("sizeCount", dsSize.size());

        // Chi tiết với tồn kho (dùng cho JS update giá + kho)
        List<Map<String,Object>> dsChiTietMap = chiTiets.stream()
                .filter(ct -> ct.getSize()!=null && ct.getMauSac()!=null)
                .map(ct -> {
                    Map<String,Object> m = new HashMap<>();
                    m.put("id", ct.getId());
                    m.put("giaBan", ct.getGiaBan());
                    m.put("size",   Map.of("id", ct.getSize().getId()));
                    m.put("mauSac", Map.of("id", ct.getMauSac().getId()));
                    m.put("soLuongTon", ct.getSoLuong());
                    return m;
                }).toList();
        model.addAttribute("dsChiTietSanPham", dsChiTietMap);


        // --- 2. TÍNH TOÁN REVIEW ---
        // 2.1: Load toàn bộ review (mới nhất trước)
        List<DanhGia> allReviews = danhGiaService.layDanhGiaChoSanPham(id);
        model.addAttribute("totalCount", allReviews.size());

        // 2.2: Đếm theo sao
        Map<Integer, Long> countByStar = Map.of(
                5, allReviews.stream().filter(r -> r.getSoSao()==5).count(),
                4, allReviews.stream().filter(r -> r.getSoSao()==4).count(),
                3, allReviews.stream().filter(r -> r.getSoSao()==3).count(),
                2, allReviews.stream().filter(r -> r.getSoSao()==2).count(),
                1, allReviews.stream().filter(r -> r.getSoSao()==1).count()
        );
        model.addAttribute("countByStar", countByStar);

        // 2.3: Tính điểm trung bình và format
        double avg = danhGiaService.tinhDiemTrungBinh(id);
        // Tách phần nguyên và phần thập phân
        int base = (int) Math.floor(avg);
        double frac = avg - base;

        String avgRatingStr = String.format(Locale.FRANCE, "%.1f", avg);
        int fullStars;
        boolean halfStar;
        // Áp theo ngưỡng: <0.3 → không half, [0.3, 0.8) → half, ≥0.8 → làm tròn lên
        if (frac < 0.3) {
            fullStars = base;
            halfStar   = false;
        } else if (frac < 0.8) {
            fullStars = base;
            halfStar   = true;
        } else {
            fullStars = base + 1;
            halfStar   = false;
        }
        int emptyStars = 5 - fullStars - (halfStar ? 1 : 0);

        model.addAttribute("avgRatingStr", avgRatingStr);
        model.addAttribute("fullStars",    fullStars);
        model.addAttribute("halfStar",     halfStar);
        model.addAttribute("emptyStars",   emptyStars);
        model.addAttribute("avgRatingStr", String.format(Locale.FRANCE, "%.1f", avg));

        // --- 3. LỌC THEO STAR VÀ PHÂN TRANG ---
        List<DanhGia> filtered = (star == null)
                ? allReviews
                : allReviews.stream()
                .filter(r -> r.getSoSao().equals(star))
                .toList();
        model.addAttribute("starFilter", star);

        int pageSize = 6;
        int total = filtered.size();
        int totalPages = (total + pageSize) / pageSize;
        int from = page * pageSize;
        int to   = Math.min(from + pageSize, total);
        List<DanhGia> pageReviews = (from < total)
                ? filtered.subList(from, to)
                : Collections.emptyList();

        model.addAttribute("reviews",      pageReviews);
        model.addAttribute("currentPage",  page);
        model.addAttribute("totalPages",   totalPages);

        return "khachhang/xemchitiet";
    }

    @GetMapping("/search")
    public String searchSanPham(
            @RequestParam("q") String q,
            Model model) {

        // Gọi service tìm theo tên chứa q
        List<SanPham> list = sanPhamRepository.search(q.trim());
        
        // Tính toán giá và dotGiamGia cho mỗi sản phẩm
        List<ChiTietSanPham> chiTiets = chitietsanphamRepo.findAll();
        
        // Set thuộc tính dotGiamGia cho mỗi sản phẩm
        Map<Integer, DotGiamGia> dotGiamGiaMap = chiTiets.stream()
            .filter(ct -> ct.getDotGiamGia() != null)
            .collect(Collectors.groupingBy(
                ct -> ct.getSanPham().getId(),
                Collectors.collectingAndThen(
                    Collectors.maxBy(Comparator.comparing(
                        ct -> ct.getDotGiamGia().getGiaTriDotGiamGia())),
                    opt -> opt.map(ChiTietSanPham::getDotGiamGia).orElse(null)
                )
            ));

        // Set dotGiamGia cho mỗi sản phẩm
        list.forEach(sanPham -> {
            sanPham.setDotGiamGia(dotGiamGiaMap.get(sanPham.getId()));
        });
        
        model.addAttribute("danhSachSanPham", list);

        // Để form binding (nếu template có dùng th:object)
        model.addAttribute("sanPham", new SanPham());

        // Giữ lại từ khoá và cờ search nếu bạn cần
        model.addAttribute("q", q);
        model.addAttribute("isSearch", true);

        return "khachhang/LISTSANPHAM";
    }



    @GetMapping("/danh-sach/filter")
    public String filterProducts(
            @RequestParam(value="q", required=false) String q,
            @RequestParam(value="danhMucId", required=false) Integer danhMucId,
            @RequestParam(value="priceRange", required=false) Integer priceRange,
            @RequestParam(value="loaiThuId", required=false) Integer loaiThuId,
            @RequestParam(value="sizeId", required=false) Integer sizeId,
            @RequestParam(value="mauSacId", required=false) Integer mauSacId,
            @RequestParam(value="kieuDangId", required=false) Integer kieuDangId,
            @RequestParam(value="thuongHieuId", required=false) Integer thuongHieuId,
            @RequestParam(value="xuatXuId", required=false) Integer xuatXuId,
            Model model
    ) {
        // Sử dụng method search mới
        List<SanPham> danhSachSanPham = sanPhamService.searchAdvanced(
            q, danhMucId, loaiThuId, sizeId, mauSacId, kieuDangId, thuongHieuId, xuatXuId, priceRange
        );

        model.addAttribute("danhSachSanPham", danhSachSanPham);

        // Tính toán giá cho mỗi sản phẩm
        List<ChiTietSanPham> chiTiets = chitietsanphamRepo.findAll();
        
        // Map giá gốc và giá bán cho mỗi sản phẩm
        Map<Integer, BigDecimal> giaGocMap = chiTiets.stream()
            .collect(Collectors.groupingBy(
                ct -> ct.getSanPham().getId(),
                Collectors.collectingAndThen(
                    Collectors.maxBy(Comparator.comparing(ChiTietSanPham::getGiaGoc)),
                    opt -> opt.map(ChiTietSanPham::getGiaGoc).orElse(BigDecimal.ZERO)
                )
            ));

        Map<Integer, BigDecimal> giaBanMap = chiTiets.stream()
            .collect(Collectors.groupingBy(
                ct -> ct.getSanPham().getId(),
                Collectors.collectingAndThen(
                    Collectors.minBy(Comparator.comparing(ChiTietSanPham::getGiaBan)),
                    opt -> opt.map(ChiTietSanPham::getGiaBan).orElse(BigDecimal.ZERO)
                )
            ));

        // Set thuộc tính dotGiamGia cho mỗi sản phẩm
        Map<Integer, DotGiamGia> dotGiamGiaMap = chiTiets.stream()
            .filter(ct -> ct.getDotGiamGia() != null)
            .collect(Collectors.groupingBy(
                ct -> ct.getSanPham().getId(),
                Collectors.collectingAndThen(
                    Collectors.maxBy(Comparator.comparing(
                        ct -> ct.getDotGiamGia().getGiaTriDotGiamGia())),
                    opt -> opt.map(ChiTietSanPham::getDotGiamGia).orElse(null)
                )
            ));

        // Set dotGiamGia cho mỗi sản phẩm
        danhSachSanPham.forEach(sanPham -> {
            sanPham.setDotGiamGia(dotGiamGiaMap.get(sanPham.getId()));
        });

        model.addAttribute("giaGocMap", giaGocMap);
        model.addAttribute("giaBanMap", giaBanMap);

        return "client/pages/product :: productGrid";
    }

    /**
     * Generate color hex code from color name if ma_mau is null/empty
     */
    private String generateColorFromName(String colorName) {
        return ColorUtil.getColorHex(colorName);
    }

    /**
     * Process colors with fallback for missing hex codes
     */
    private List<MauSac> processColorsWithFallback(List<MauSac> mauSacs) {
        return mauSacs.stream()
                .peek(mauSac -> {
                    if (mauSac.getMaMau() == null || mauSac.getMaMau().trim().isEmpty()) {
                        // Auto-generate color based on name if not set
                        String generatedColor = generateColorFromName(mauSac.getTen());
                        mauSac.setMaMau(generatedColor);
                        System.out.println("Generated color for '" + mauSac.getTen() + "': " + generatedColor);
                    }
                })
                .collect(Collectors.toList());
    }
}
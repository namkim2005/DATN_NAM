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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.Normalizer;
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
        @RequestParam(value="sortBy", required=false) String sortBy,
        @RequestParam(value="sortDir", required=false) String sortDir,

            Model model
    ) {
        if (getKhachHang.getCurrentKhachHang() != null) {
            Integer currentId = getKhachHang.getCurrentKhachHang().getId();
            model.addAttribute("idKhachHang", currentId);
        }
        // Sử dụng method search mới
        List<SanPham> danhSachSanPham = sanPhamService.searchAdvanced(
            q, danhMucId, loaiThuId, sizeId, mauSacId, kieuDangId, thuongHieuId, xuatXuId, priceRange, sortBy, sortDir
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



        // Lấy giá trực tiếp từ database (giaBan đã được tính toán sẵn)
        List<ChiTietSanPham> chiTiets = chitietsanphamRepo.findAll();
        
        // Lấy giá bán và giá gốc cho biến thể rẻ nhất
        Map<Integer, BigDecimal> giaBanMinMap = new HashMap<>();
        Map<Integer, BigDecimal> giaGocCuaBienTheReNhatMap = new HashMap<>();
        Map<Integer, String> tenDotGiamGiaMap = new HashMap<>();
        for (SanPham sp : danhSachSanPham) {
            List<ChiTietSanPham> list = chiTiets.stream().filter(ct -> ct.getSanPham().getId().equals(sp.getId())).toList();
            ChiTietSanPham minCt = null;
            BigDecimal minGia = null;
            for (ChiTietSanPham ct : list) {
                BigDecimal giaGoc = ct.getGiaGoc() == null ? BigDecimal.ZERO : ct.getGiaGoc();
                // Lấy trực tiếp giaBan từ database (đã được tính toán sẵn)
                BigDecimal giaBan = ct.getGiaBan() != null ? ct.getGiaBan() : giaGoc;
                
                if (minGia == null || giaBan.compareTo(minGia) < 0) {
                    minGia = giaBan;
                    minCt = ct;
                }
            }
            giaBanMinMap.put(sp.getId(), minGia == null ? BigDecimal.ZERO : minGia);
            if (minCt != null) {
                giaGocCuaBienTheReNhatMap.put(sp.getId(), minCt.getGiaGoc() == null ? BigDecimal.ZERO : minCt.getGiaGoc());
                tenDotGiamGiaMap.put(sp.getId(), minCt.getDotGiamGia() != null ? minCt.getDotGiamGia().getTen() : null);
            }
        }

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

        model.addAttribute("giaGocMinVariantMap", giaGocCuaBienTheReNhatMap);
        model.addAttribute("giaBanMinMap", giaBanMinMap);
        model.addAttribute("tenDotGiamGiaMap", tenDotGiamGiaMap);

        // Debug log để kiểm tra dữ liệu giá
        System.out.println("=== DEBUG GIÁ SẢN PHẨM ===");
        System.out.println("giaBanMinMap size: " + (giaBanMinMap != null ? giaBanMinMap.size() : "null"));
        System.out.println("giaGocMinVariantMap size: " + (giaGocCuaBienTheReNhatMap != null ? giaGocCuaBienTheReNhatMap.size() : "null"));
        System.out.println("tenDotGiamGiaMap size: " + (tenDotGiamGiaMap != null ? tenDotGiamGiaMap.size() : "null"));
        
        // Log chi tiết cho một vài sản phẩm đầu tiên
        if (danhSachSanPham.size() > 0) {
            SanPham firstSp = danhSachSanPham.get(0);
            Integer spId = firstSp.getId();
            System.out.println("Sản phẩm đầu tiên ID: " + spId);
            System.out.println("  - giaBan: " + (giaBanMinMap != null ? giaBanMinMap.get(spId) : "null"));
            System.out.println("  - giaGoc: " + (giaGocCuaBienTheReNhatMap != null ? giaGocCuaBienTheReNhatMap.get(spId) : "null"));
            System.out.println("  - tenDot: " + (tenDotGiamGiaMap != null ? tenDotGiamGiaMap.get(spId) : "null"));
        }
        System.out.println("=== END DEBUG ===");

        // Tính điểm trung bình và tổng số lượng tồn theo sản phẩm
        Map<Integer, Double> avgRatingMap = danhSachSanPham.stream()
                .collect(Collectors.toMap(
                        SanPham::getId,
                        sp -> {
                            double avg = danhGiaService.tinhDiemTrungBinh(sp.getId());
                            if (avg == 0 && (sp.getDanhGias() == null || sp.getDanhGias().isEmpty())) return 5.0;
                            return avg;
                        }
                ));
        Map<Integer, Integer> totalQuantityMap = chitietsanphamRepo.findAll().stream()
                .collect(Collectors.groupingBy(ct -> ct.getSanPham().getId(),
                        Collectors.summingInt(ct -> ct.getSoLuong() == null ? 0 : ct.getSoLuong())));
        model.addAttribute("avgRatingMap", avgRatingMap);
        model.addAttribute("totalQuantityMap", totalQuantityMap);

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
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "client/pages/product/product";
    }


    @GetMapping("/chi-tiet/{id}")
    public String xemChiTietSanPham(
            @PathVariable("id") Integer id,
            @RequestParam(value="page", defaultValue="0") int page,
            @RequestParam(value="star", required=false) Integer star,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            System.out.println("=== ENTERING PRODUCT DETAIL CONTROLLER ===");
            System.out.println("Product ID: " + id);
            
            // --- 1. LOAD SẢN PHẨM, HÌNH, SIZE, MÀU ---
            SanPham sanPham = sanPhamService.findbyid(id);
            System.out.println("SanPham found: " + (sanPham != null));
            if (sanPham == null) {
                System.out.println("SanPham is null, redirecting...");
                return "redirect:/san-pham/danh-sach";
            }
        
        model.addAttribute("sanPham", sanPham);
        // Lấy hình ảnh của sản phẩm
        List<HinhAnh> hinhanhs = hinhanhrepository.findByhinhanhid(id);
        HinhAnh hinhanh = hinhanhs.isEmpty() ? null : hinhanhs.get(0);
        model.addAttribute("hinhanh", hinhanh);
        model.addAttribute("hinhanhs", hinhanhs); // Để hiển thị nhiều ảnh nếu cần

        List<ChiTietSanPham> chiTiets = chitietsanphamRepo.findBySanPhamId(id);
        
        // Kiểm tra nếu không có ChiTietSanPham
        if (chiTiets.isEmpty()) {
            // Tạm thời comment redirect để debug
            System.out.println("WARNING: No ChiTietSanPham found for product ID: " + id);
            // redirectAttributes.addFlashAttribute("error", "Sản phẩm này chưa có thông tin chi tiết!");
            // return "redirect:/san-pham/danh-sach";
        }
        
        // Tính toán giá từ các biến thể
        BigDecimal giaBanMin = chiTiets.stream()
                .map(ChiTietSanPham::getGiaBan)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
                
        BigDecimal giaGocMin = chiTiets.stream()
                .map(ChiTietSanPham::getGiaGoc)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        model.addAttribute("giaBanMin", giaBanMin);
        model.addAttribute("giaGocMin", giaGocMin);

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
        
        // Màu sắc duy nhất
        List<MauSac> dsMauSac = chiTiets.stream()
                .map(ChiTietSanPham::getMauSac)
                .filter(ms -> ms != null && ms.getId() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(MauSac::getId, Function.identity(), (a,b)->a),
                        m -> new ArrayList<>(m.values())
                ));
        
        // Xử lý màu sắc với fallback cho mã màu
        dsMauSac = processColorsWithFallback(dsMauSac);
        
        model.addAttribute("dsMauSac", dsMauSac);
        model.addAttribute("mauSacCount", dsMauSac.size());
        
        // Debug logging
        System.out.println("=== DEBUG PRODUCT DETAIL ===");
        System.out.println("Total ChiTietSanPham: " + chiTiets.size());
        System.out.println("Size count: " + dsSize.size());
        System.out.println("Color count: " + dsMauSac.size());
        System.out.println("GiaBanMin: " + giaBanMin);
        System.out.println("GiaGocMin: " + giaGocMin);
        chiTiets.forEach(ct -> {
            System.out.println("ChiTiet ID: " + ct.getId() + 
                             ", Size: " + (ct.getSize() != null ? ct.getSize().getTen() : "NULL") +
                             ", Color: " + (ct.getMauSac() != null ? ct.getMauSac().getTen() : "NULL") +
                             ", GiaBan: " + ct.getGiaBan() +
                             ", SoLuong: " + ct.getSoLuong());
        });
        System.out.println("=== END DEBUG ===");

        // Chi tiết với tồn kho (dùng cho JS update giá + kho)
        List<Map<String,Object>> dsChiTietMap = chiTiets.stream()
                .filter(ct -> ct.getSize()!=null && ct.getMauSac()!=null)
                .map(ct -> {
                    Map<String,Object> m = new HashMap<>();
                    m.put("id", ct.getId());
                    m.put("giaBan", ct.getGiaBan());
                    m.put("giaGoc", ct.getGiaGoc()); // Thêm giá gốc
                    m.put("size",   Map.of("id", ct.getSize().getId(), "ten", ct.getSize().getTen()));
                    m.put("mauSac", Map.of("id", ct.getMauSac().getId(), "ten", ct.getMauSac().getTen()));
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

        System.out.println("=== RETURNING TEMPLATE ===");
        return "client/pages/product/product-detail";
        } catch (Exception e) {
            System.out.println("=== ERROR IN CONTROLLER ===");
            e.printStackTrace();
            return "redirect:/san-pham/danh-sach";
        }
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

        return "client/pages/product/search";
    }

    @GetMapping("/debug-gia")
    public String debugGiaSanPham(Model model) {
        // Sử dụng method search mới
        List<SanPham> danhSachSanPham = sanPhamService.searchAdvanced(
            null, null, null, null, null, null, null, null, null, null, null
        );
        model.addAttribute("danhSachSanPham", danhSachSanPham);

        // Lấy giá trực tiếp từ database (giaBan đã được tính toán sẵn)
        List<ChiTietSanPham> chiTiets = chitietsanphamRepo.findAll();
        
        // Lấy giá bán và giá gốc cho biến thể rẻ nhất
        Map<Integer, BigDecimal> giaBanMinMap = new HashMap<>();
        Map<Integer, BigDecimal> giaGocCuaBienTheReNhatMap = new HashMap<>();
        Map<Integer, String> tenDotGiamGiaMap = new HashMap<>();
        
        for (SanPham sp : danhSachSanPham) {
            List<ChiTietSanPham> list = chiTiets.stream().filter(ct -> ct.getSanPham().getId().equals(sp.getId())).toList();
            ChiTietSanPham minCt = null;
            BigDecimal minGia = null;
            for (ChiTietSanPham ct : list) {
                BigDecimal giaGoc = ct.getGiaGoc() == null ? BigDecimal.ZERO : ct.getGiaGoc();
                // Lấy trực tiếp giaBan từ database (đã được tính toán sẵn)
                BigDecimal giaBan = ct.getGiaBan() != null ? ct.getGiaBan() : giaGoc;
                
                if (minGia == null || giaBan.compareTo(minGia) < 0) {
                    minGia = giaBan;
                    minCt = ct;
                }
            }
            giaBanMinMap.put(sp.getId(), minGia == null ? BigDecimal.ZERO : minGia);
            if (minCt != null) {
                giaGocCuaBienTheReNhatMap.put(sp.getId(), minCt.getGiaGoc() == null ? BigDecimal.ZERO : minCt.getGiaGoc());
                tenDotGiamGiaMap.put(sp.getId(), minCt.getDotGiamGia() != null ? minCt.getDotGiamGia().getTen() : null);
            }
        }

        model.addAttribute("giaGocMinVariantMap", giaGocCuaBienTheReNhatMap);
        model.addAttribute("giaBanMinMap", giaBanMinMap);
        model.addAttribute("tenDotGiamGiaMap", tenDotGiamGiaMap);

        // Debug log chi tiết
        System.out.println("=== DEBUG GIÁ SẢN PHẨM (DEBUG PAGE) ===");
        System.out.println("giaBanMinMap: " + giaBanMinMap);
        System.out.println("giaGocMinVariantMap: " + giaGocCuaBienTheReNhatMap);
        System.out.println("tenDotGiamGiaMap: " + tenDotGiamGiaMap);
        System.out.println("=== END DEBUG ===");

        return "client/pages/product/product-debug";
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
            @RequestParam(value="sortBy", required=false) String sortBy,
            @RequestParam(value="sortDir", required=false) String sortDir,
            Model model
    ) {
        // Sử dụng method search mới
        List<SanPham> danhSachSanPham = sanPhamService.searchAdvanced(
            q, danhMucId, loaiThuId, sizeId, mauSacId, kieuDangId, thuongHieuId, xuatXuId, priceRange, sortBy, sortDir
        );

        model.addAttribute("danhSachSanPham", danhSachSanPham);

        // Tính toán giá cho mỗi sản phẩm
        List<ChiTietSanPham> chiTiets = chitietsanphamRepo.findAll();
        
        // Lấy giá bán và giá gốc cho biến thể rẻ nhất
        Map<Integer, BigDecimal> giaBanMinMap = new HashMap<>();
        Map<Integer, BigDecimal> giaGocCuaBienTheReNhatMap = new HashMap<>();
        Map<Integer, String> tenDotGiamGiaMap = new HashMap<>();
        for (SanPham sp : danhSachSanPham) {
            List<ChiTietSanPham> list = chiTiets.stream().filter(ct -> ct.getSanPham().getId().equals(sp.getId())).toList();
            ChiTietSanPham minCt = null;
            BigDecimal minGia = null;
            for (ChiTietSanPham ct : list) {
                BigDecimal giaGoc = ct.getGiaGoc() == null ? BigDecimal.ZERO : ct.getGiaGoc();
                BigDecimal giaBan = giaGoc;
                if (ct.getDotGiamGia() != null && giaGoc != null) {
                    DotGiamGia dgg = ct.getDotGiamGia();
                    BigDecimal giaTri = dgg.getGiaTriDotGiamGia();
                    if (giaTri != null) {
                        if ("TIEN".equalsIgnoreCase(dgg.getLoai())) {
                            giaBan = giaGoc.subtract(giaTri);
                        } else {
                            BigDecimal phanTram = giaTri.divide(new BigDecimal(100), 2, java.math.RoundingMode.HALF_UP);
                            giaBan = giaGoc.subtract(giaGoc.multiply(phanTram));
                        }
                        if (giaBan.compareTo(BigDecimal.ZERO) < 0) giaBan = BigDecimal.ZERO;
                    }
                }
                if (minGia == null || giaBan.compareTo(minGia) < 0) {
                    minGia = giaBan;
                    minCt = ct;
                }
            }
            giaBanMinMap.put(sp.getId(), minGia == null ? BigDecimal.ZERO : minGia);
            if (minCt != null) {
                giaGocCuaBienTheReNhatMap.put(sp.getId(), minCt.getGiaGoc() == null ? BigDecimal.ZERO : minCt.getGiaGoc());
                tenDotGiamGiaMap.put(sp.getId(), minCt.getDotGiamGia() != null ? minCt.getDotGiamGia().getTen() : null);
            }
        }

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

        model.addAttribute("giaGocMinVariantMap", giaGocCuaBienTheReNhatMap);
        model.addAttribute("giaBanMinMap", giaBanMinMap);
        model.addAttribute("tenDotGiamGiaMap", tenDotGiamGiaMap);

        // Ảnh: ưu tiên loai_anh = 0, sau đó tên "ảnh chính", cuối cùng ảnh đầu
        Map<Integer, String> firstImageUrlMap = new HashMap<>();
        for (SanPham sp : danhSachSanPham) {
            List<HinhAnh> images = hinhanhrepository.findByhinhanhid(sp.getId());
            String url = null;
            if (images != null) {
                Optional<HinhAnh> mainFlag = images.stream()
                        .filter(i -> i.getLoaiAnh() != null && i.getLoaiAnh() == 0)
                        .findFirst();
                if (mainFlag.isPresent()) {
                    url = mainFlag.get().getUrl();
                }
            }
            if (url != null && !url.isBlank()) {
                url = url.replace("\\", "/");
                if (!url.startsWith("http") && !url.startsWith("/")) {
                    url = "/" + url;
                }
                firstImageUrlMap.put(sp.getId(), url);
            }
        }
        model.addAttribute("firstImageUrlMap", firstImageUrlMap);

        // Tính điểm trung bình và tổng số lượng tồn theo sản phẩm
        Map<Integer, Double> avgRatingMap = danhSachSanPham.stream()
                .collect(Collectors.toMap(
                        SanPham::getId,
                        sp -> danhGiaService.tinhDiemTrungBinh(sp.getId())
                ));
        Map<Integer, Integer> totalQuantityMap = chitietsanphamRepo.findAll().stream()
                .collect(Collectors.groupingBy(ct -> ct.getSanPham().getId(),
                        Collectors.summingInt(ct -> ct.getSoLuong() == null ? 0 : ct.getSoLuong())));
        model.addAttribute("avgRatingMap", avgRatingMap);
        model.addAttribute("totalQuantityMap", totalQuantityMap);

        // Map sản phẩm đã yêu thích của khách hàng hiện tại
        Map<Integer, Boolean> likedMap = new HashMap<>();
        if (getKhachHang.getCurrentKhachHang() != null) {
            Integer currentId = getKhachHang.getCurrentKhachHang().getId();
            for (SanPham sp : danhSachSanPham) {
                boolean liked = sp.getSpYeuThiches().stream().anyMatch(y -> y.getKhachHang().getId().equals(currentId));
                likedMap.put(sp.getId(), liked);
            }
        }
        model.addAttribute("likedMap", likedMap);

        return "client/pages/product/product :: productGrid";
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

    private String normalize(String input) {
        if (input == null) return "";
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        return temp.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
    }
}
package com.main.datn_sd31.controller.admin_controller;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonChiTietDTO;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.entity.*;
import com.main.datn_sd31.repository.*;
import com.main.datn_sd31.service.HoaDonChiTietService;
import com.main.datn_sd31.service.HoaDonService;
import com.main.datn_sd31.service.PhieuGiamGiaService;
import com.main.datn_sd31.service.impl.GHNService;
import com.main.datn_sd31.util.GetNhanVien;
import com.main.datn_sd31.util.ThongBaoUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/ban-hang")
@RequiredArgsConstructor
@Transactional
public class BanHangController {

    private final GetNhanVien getNhanVien;
    private final Chitietsanphamrepository chiTietSanPhamRepository;
    private final HoaDonRepository hoaDonRepository;
    private final Chitiethoadonrepository hoaDonChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final SanPhamRepository sanphamrepository;
    private final PhieuGiamGiaRepository phieugiamgiarepository;
    private final GHNService ghnService;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final Mausacrepository mausacrepository;
    private final Sizerepository sizerepository;
    private final PhieuGiamGiaService phieuGiamGiaService;
    private final HoaDonService hoaDonService;
    private final HoaDonChiTietService hoaDonChiTietService;


    //    private List<HoaDonChiTiet> getCart(String cartKey, HttpSession session) {
//        Map<String, List<HoaDonChiTiet>> carts = (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
//        if (carts == null) {
//            carts = new HashMap<>();
//            session.setAttribute("tatCaGio", carts);
//        }
//        return carts.computeIfAbsent(cartKey, k -> new ArrayList<>());
//    }
    @SuppressWarnings("unchecked")
    private List<HoaDonChiTiet> getCart(String cartKey, HttpSession session) {
        Map<String, List<HoaDonChiTiet>> allCarts = (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
        if (allCarts == null) {
            allCarts = new HashMap<>();
            session.setAttribute("tatCaGio", allCarts);
        }

        return allCarts.computeIfAbsent(cartKey, k -> new ArrayList<>());
    }

    private String findEmptyCartKey(Set<String> existingKeys) {
        for (int i = 1; i <= 5; i++) {
            String key = "gio-" + i;
            if (!existingKeys.contains(key)) {
                return key;
            }
        }
        return null; // Không còn giỏ trống
    }
    public PhieuGiamGia timPhieuTotNhat(List<PhieuGiamGia> dsPhieu, BigDecimal tongTien) {
        return dsPhieu.stream()
                .filter(p -> p.getNgayKetThuc().isAfter(LocalDate.now())) // còn hạn
                .filter(p -> tongTien.compareTo(p.getDieuKien()) >= 0)    // đủ điều kiện
                .max(Comparator.comparing(p -> {
                    BigDecimal soTienGiamThucTe = BigDecimal.ZERO; // giá trị mặc định

                    if (p.getLoaiPhieuGiamGia() == 1) {
                        // Giảm theo %
                        BigDecimal giamTheoPhanTram = tongTien.multiply(p.getMucDo())
                                .divide(BigDecimal.valueOf(100));
                        soTienGiamThucTe = giamTheoPhanTram.min(p.getGiamToiDa());
                    } else if (p.getLoaiPhieuGiamGia() == 2) {
                        // Giảm theo số tiền cố định
                        soTienGiamThucTe = p.getMucDo();
                    }

                    return soTienGiamThucTe;
                }))
                .orElse(null);
    }


    @GetMapping
    public String hienThiSanPham(
            @RequestParam(value = "idSanPham", required = false) Integer idSanPham,
            @RequestParam(value = "cartKey", defaultValue = "gio-1") String cartKey,
            @ModelAttribute("successMessage") String successMessage,
            @ModelAttribute("errorMessage") String errorMessage,
            @ModelAttribute("soDienThoaiMoi") String soDienThoaiMoi,
            Model model, HttpSession session) {

        // Lấy tất cả giỏ từ session
        Map<String, List<HoaDonChiTiet>> carts =
                (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
        if (carts == null) {
            carts = new HashMap<>();
            session.setAttribute("tatCaGio", carts);
        }

        // Tìm cartKey trống để hiển thị cho nút "+ Giỏ mới"
        String nextCartKey = findEmptyCartKey(carts.keySet());
        model.addAttribute("nextCartKey", nextCartKey);

        // Danh sách sản phẩm & chi tiết sản phẩm
        List<SanPham> dsSanPham = sanphamrepository.findAll();
        List<ChiTietSanPham> dsChiTiet = idSanPham != null
                ? chiTietSanPhamRepository.findBySanPhamId(idSanPham)
                : new ArrayList<>();

        // Lấy giỏ hàng hiện tại
        List<HoaDonChiTiet> gio = getCart(cartKey, session);

        // Tính tổng tiền
        BigDecimal tongTien = gio.stream()
                .map(i -> i.getChiTietSanPham().getGiaBan().multiply(BigDecimal.valueOf(i.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy phí ship & giảm giá của giỏ hiện tại từ session (theo cartKey)
        BigDecimal phiShip = Optional.ofNullable((BigDecimal) session.getAttribute("phiVanChuyen_" + cartKey))
                .orElse(BigDecimal.ZERO);
        BigDecimal giamGia = Optional.ofNullable((BigDecimal) session.getAttribute("giamGia_" + cartKey))
                .orElse(BigDecimal.ZERO);
        Object maGiamGia = session.getAttribute("maGiamGia_" + cartKey);

        // Nếu tổng tiền <= 0 thì reset riêng giỏ này
        // Nếu có mã giảm giá thì kiểm tra lại điều kiện
        if (maGiamGia != null) {
            PhieuGiamGia phieu = phieugiamgiarepository.findByMa(maGiamGia.toString());

            boolean hopLe = true;
            if (phieu == null) {
                hopLe = false; // không tồn tại
            } else {
                // Nếu giỏ rỗng hoặc tổng tiền < điều kiện tối thiểu
                if (gio.isEmpty() || tongTien.compareTo(phieu.getDieuKien()) < 0) {
                    hopLe = false;
                }
            }

            // Nếu mã không hợp lệ thì xóa
            if (!hopLe) {
                giamGia = BigDecimal.ZERO;
                maGiamGia = null;

                session.setAttribute("giamGia_" + cartKey, giamGia);
                session.removeAttribute("maGiamGia_" + cartKey);
            }
        }

        if (gio.isEmpty()) {
            giamGia = BigDecimal.ZERO;
            phiShip = BigDecimal.ZERO;
            maGiamGia = null;

            session.setAttribute("giamGia_" + cartKey, giamGia);
            session.setAttribute("phiVanChuyen_" + cartKey, phiShip);
            session.removeAttribute("maGiamGia_" + cartKey);
        }


        // Tính tổng sau giảm
        BigDecimal tongSauGiam = tongTien.subtract(giamGia).add(phiShip);
        if (tongTien.compareTo(BigDecimal.ZERO) <= 0) {
            tongSauGiam = BigDecimal.ZERO;
        }

        // Lấy danh sách phiếu giảm giá hợp lệ
        LocalDate today = LocalDate.now();
        List<PhieuGiamGia> dsPhieuGiamGia = phieugiamgiarepository.findAll().stream()
                .filter(phieu -> Boolean.TRUE.equals(phieu.getTrangThai()))
                .filter(phieu -> phieu.getSoLuongTon() != null && phieu.getSoLuongTon() > 0)
                .filter(phieu -> (phieu.getNgayBatDau() == null || !today.isBefore(phieu.getNgayBatDau())))
                .filter(phieu -> (phieu.getNgayKetThuc() == null || !today.isAfter(phieu.getNgayKetThuc())))
                .collect(Collectors.toList());

        // Đẩy dữ liệu ra model
        model.addAttribute("dsSanPham", dsSanPham);
        model.addAttribute("dsChiTietSanPham", dsChiTiet);
        model.addAttribute("sanPhamDaChon", idSanPham);
        model.addAttribute("gioHang", gio);
        model.addAttribute("tatCaGio", carts.keySet());
        model.addAttribute("cartKey", cartKey);
        model.addAttribute("tongTien", tongTien);

        model.addAttribute("giamGia", giamGia);
        model.addAttribute("phiVanChuyen", phiShip);
        model.addAttribute("maGiamGia", maGiamGia);
        model.addAttribute("tongTienSauGiam", tongSauGiam);

        model.addAttribute("dsPhieuGiamGia", dsPhieuGiamGia);

        PhieuGiamGia phieuTotNhat = timPhieuTotNhat(dsPhieuGiamGia, tongTien);
        model.addAttribute("phieuTotNhat", phieuTotNhat);

        List<KhachHang> dsKhachHang = khachHangRepository.findAll();
        model.addAttribute("dsKhachHang", dsKhachHang);
        if (successMessage != null) {
            model.addAttribute("successMessage", successMessage);
        }
        if (errorMessage != null) {
            model.addAttribute("errorMessage", errorMessage);
        }
        if (soDienThoaiMoi != null) {
            model.addAttribute("soDienThoaiMoi", soDienThoaiMoi);
        }
        return "admin/pages/banhang/banhang";
    }


    @PostMapping("/ap-dung-ma")
    public String apDungMa(@RequestParam("maGiamGia") String ma,
                           @RequestParam("cartKey") String cartKey,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        List<HoaDonChiTiet> gio = getCart(cartKey, session);

        if (gio == null || gio.isEmpty()) {
            ThongBaoUtils.addError(redirectAttributes, "Giỏ hàng trống, không thể áp mã.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        BigDecimal tongTien = gio.stream()
                .map(i -> i.getChiTietSanPham().getGiaBan().multiply(BigDecimal.valueOf(i.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PhieuGiamGia phieu = phieugiamgiarepository.findByMa(ma.trim());

        if (phieu == null) {
            ThongBaoUtils.addError(redirectAttributes, "Mã giảm giá không tồn tại.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        LocalDate today = LocalDate.now();

        // Kiểm tra điều kiện áp dụng
        if (!Boolean.TRUE.equals(phieu.getTrangThai())) {
            ThongBaoUtils.addError(redirectAttributes, "Mã giảm giá không khả dụng.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }
        if (phieu.getNgayBatDau() != null && today.isBefore(phieu.getNgayBatDau())) {
            ThongBaoUtils.addError(redirectAttributes, "Mã giảm giá chưa bắt đầu.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }
        if (phieu.getNgayKetThuc() != null && today.isAfter(phieu.getNgayKetThuc())) {
            ThongBaoUtils.addError(redirectAttributes, "Mã giảm giá đã hết hạn.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }
        if (phieu.getSoLuongTon() != null && phieu.getSoLuongTon() <= 0) {
            ThongBaoUtils.addError(redirectAttributes, "Mã giảm giá đã hết lượt sử dụng.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }
        if (phieu.getDieuKien() != null && tongTien.compareTo(phieu.getDieuKien()) < 0) {
            ThongBaoUtils.addError(redirectAttributes, "Đơn hàng chưa đạt giá trị tối thiểu để áp mã.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        // Tính mức giảm
        BigDecimal tienGiam = BigDecimal.ZERO;
        if (phieu.getLoaiPhieuGiamGia() == 2) { // giảm cố định
            tienGiam = phieu.getMucDo();
        } else if (phieu.getLoaiPhieuGiamGia() == 1) { // giảm %
            tienGiam = tongTien.multiply(phieu.getMucDo()).divide(BigDecimal.valueOf(100));
            if (phieu.getGiamToiDa() != null && phieu.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
                tienGiam = tienGiam.min(phieu.getGiamToiDa());
            }
        }

        // Lưu theo cartKey
        session.setAttribute("giamGia_" + cartKey, tienGiam);
        session.setAttribute("maGiamGia_" + cartKey, ma.trim());


        ThongBaoUtils.addSuccess(redirectAttributes, "Áp dụng mã giảm giá thành công!");
        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }

    @PostMapping("/xoa-ma")
    public String xoaMa(@RequestParam("cartKey") String cartKey,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        session.removeAttribute("maGiamGia_" + cartKey);
        session.setAttribute("giamGia_" + cartKey, BigDecimal.ZERO);

        ThongBaoUtils.addSuccess(redirectAttributes, "Đã xóa mã giảm giá khỏi giỏ hàng.");

        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }



    @PostMapping("/xoa-gio")
    public String xoaGioHang(@RequestParam("cartKey") String cartKey, HttpSession session) {
        Map<String, List<HoaDonChiTiet>> carts = (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
        if (carts != null) {
            carts.remove(cartKey);
            session.setAttribute("tatCaGio", carts); // Cập nhật lại session

            // Xóa thông tin liên quan
            session.removeAttribute("phiVanChuyen");
            session.removeAttribute("maGiamGia");

            // Nếu còn giỏ, redirect về giỏ đầu tiên
            if (!carts.isEmpty()) {
                String newCartKey = carts.keySet().iterator().next(); // giỏ đầu tiên còn lại
                return "redirect:/admin/ban-hang?cartKey=" + newCartKey;
            }
        }

        // Nếu không còn giỏ nào, tạo giỏ mới
        return "redirect:/admin/ban-hang?cartKey=gio-1";
    }

    // Generate mã khách hàng tự động
    private String generateMaKhachHang() {
        String prefix = "KH";
        List<KhachHang> allKhachHang = khachHangRepository.findAll();

        int maxNumber = 0;
        for (KhachHang kh : allKhachHang) {
            String ma = kh.getMa();
            if (ma != null && ma.startsWith(prefix)) {
                try {
                    int number = Integer.parseInt(ma.substring(prefix.length()));
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException e) {
                    // Ignore invalid format
                }
            }
        }

        return prefix + String.format("%03d", maxNumber + 1);
    }

    @PostMapping("/them-khach-hang-nhanh")
    public String themNhanhKhachHang(@ModelAttribute KhachHang kh,
                                     @RequestParam("cartKey") String cartKey,
                                     RedirectAttributes redirectAttributes) {
        // Check số điện thoại trùng
        if (khachHangRepository.existsBySoDienThoai(kh.getSoDienThoai())) {
            ThongBaoUtils.addError(redirectAttributes, "Số điện thoại đã tồn tại!");
            redirectAttributes.addFlashAttribute("soDienThoaiMoi", kh.getSoDienThoai());
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }
        if (kh.getSoDienThoai().length()!=10 && !kh.getSoDienThoai().matches("0\\d{9}")) {
            ThongBaoUtils.addError(redirectAttributes, "Số điện thoại không hợp lệ! Phải đúng 10 chữ số và bắt đầu bằng 0.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        // Tạo mã KH tự động
        kh.setMa(generateMaKhachHang());
        kh.setNgayThamGia(LocalDateTime.now());
        kh.setNgayTao(LocalDateTime.now());
        kh.setTrangThai(true);

        if (kh.getEmail() == null) kh.setEmail("");
        if (kh.getDiaChi() == null) kh.setDiaChi("");

        khachHangRepository.save(kh);

        redirectAttributes.addFlashAttribute("soDienThoaiMoi", kh.getSoDienThoai());
        ThongBaoUtils.addSuccess(redirectAttributes, "Thêm khách hàng thành công!");
        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }


    @GetMapping("/tim-kiem-san-pham")
    @ResponseBody
    public List<Map<String, Object>> timKiemSanPham(@RequestParam("keyword") String keyword) {
        List<ChiTietSanPham> danhSach = chiTietSanPhamRepository.searchByKeywordSplit( keyword);

        List<Map<String, Object>> ketQua = new ArrayList<>();

        for (ChiTietSanPham ctsp : danhSach) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", ctsp.getId()); // ID của ChiTietSanPham
            map.put("ma", ctsp.getSanPham().getMa());
            map.put("tenSanPham", ctsp.getSanPham().getTen());

            // Hình ảnh đại diện (lấy cái đầu tiên nếu có)
            String hinhAnh = ctsp.getSanPham().getHinhAnhs().stream()
                    .findFirst()
                    .map(HinhAnh::getUrl)
                    .orElse("/images/no-image.png");
            map.put("hinhAnh", hinhAnh);

            // Thông tin màu, size và tồn kho
            map.put("mauSac", ctsp.getMauSac().getTen());
            map.put("kichThuoc", ctsp.getSize().getTen());
            map.put("soLuong", ctsp.getSoLuong());
            map.put("giaBan", ctsp.getGiaBan());
            map.put("trangThaiHoatDong", ctsp.getSanPham().getTrangThai());
//            System.out.println(ctsp.getTrangThai());

            ketQua.add(map);
        }

        return ketQua;
    }

    @PostMapping("/cap-nhat-so-luong")
    public String capNhatSoLuong(@RequestParam("idChiTietSp") Integer id,
                                 @RequestParam("soLuong") Integer soLuong,
                                 @RequestParam("cartKey") String cartKey,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        ChiTietSanPham ctsp = chiTietSanPhamRepository.findWithDetailsById(id);
        if (ctsp == null) {
            redirectAttributes.addFlashAttribute("error", "❌ Không tìm thấy sản phẩm.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        // Kiểm tra tồn kho
        Integer tonKho = (ctsp.getSoLuong() == null ? 0 : ctsp.getSoLuong());
        if (soLuong == null || soLuong <= 0) {
            redirectAttributes.addFlashAttribute("error", "❌ Số lượng không hợp lệ.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }
        if (soLuong > tonKho) {
            redirectAttributes.addFlashAttribute("error",
                    "❌ Số lượng vượt quá tồn kho (tồn: " + tonKho + ").");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        // Cập nhật vào giỏ
        List<HoaDonChiTiet> gio = getCart(cartKey, session);
        gio.stream()
                .filter(item -> item.getChiTietSanPham().getId().intValue() == id)
                .findFirst()
                .ifPresent(item -> item.setSoLuong(soLuong));

        redirectAttributes.addFlashAttribute("success", "✅ Cập nhật số lượng thành công");
        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }



    @GetMapping("/tim-kiem")
    public String timKiemSanPham(@RequestParam("keyword") String keyword,
                                 @RequestParam("cartKey") String cartKey,
                                 Model model) {
        ChiTietSanPham ketQua = chiTietSanPhamRepository.findByMaVach(keyword);
        model.addAttribute("ketQua", ketQua);
        model.addAttribute("ten",ketQua.getTenCt());
        model.addAttribute("mausac",ketQua.getMauSac().getTen());
        model.addAttribute("cartKey", cartKey);
        return "admin/banhang"; // trang bán hàng hiển thị luôn kết quả tìm
    }

    @GetMapping("/tim-kiem-theo-ma-vach")
    @ResponseBody
    public Map<String, Object> timKiemSanPhamJson(@RequestParam("maVach") String maVach) {
        Map<String, Object> result = new HashMap<>();
        ChiTietSanPham sp = chiTietSanPhamRepository.findByMaVach(maVach);
        if (sp != null) {
            result.put("tenSanPham", sp.getSanPham().getTen());
            result.put("mauSac", sp.getMauSac().getTen());
            result.put("size", sp.getSize().getTen());
            result.put("soluong", sp.getSoLuong());
            result.put("gia", sp.getGiaBan());
            result.put("idChiTietSp", sp.getId()); // Thêm ID nếu cần xử lý tiếp
        }
        return result;
    }

    @PostMapping("/them-gio")
    public String themVaoGio(@RequestParam("idChiTietSp") Integer id,
                             @RequestParam("soLuong") Integer soLuong,
                             @RequestParam("cartKey") String cartKey,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        ChiTietSanPham ctsp = chiTietSanPhamRepository.findWithDetailsById(id);
        if (ctsp == null) {
            ThongBaoUtils.addError(redirectAttributes, "Không tìm thấy sản phẩm");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        List<HoaDonChiTiet> gio = getCart(cartKey, session);

        Optional<HoaDonChiTiet> existingItem = gio.stream()
                .filter(i -> i.getChiTietSanPham().getId().equals(id))
                .findFirst();

        int tongSoLuongMuonThem = soLuong + existingItem.map(HoaDonChiTiet::getSoLuong).orElse(0);
        if (tongSoLuongMuonThem > ctsp.getSoLuong()) {
            ThongBaoUtils.addError(redirectAttributes, "Sản phẩm không đủ tồn kho");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        if (existingItem.isPresent()) {
            existingItem.get().setSoLuong(tongSoLuongMuonThem);
        } else {
            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setChiTietSanPham(ctsp);
            ct.setSoLuong(soLuong);
            ct.setGiaGiam(BigDecimal.ZERO);
            ct.setGiaSauGiam(ctsp.getGiaBan());
            ct.setTrangThai(true);
            ct.setNgayTao(LocalDateTime.now());
            ct.setNguoiTao(getNhanVien.getCurrentNhanVien().getId());
            ct.setTenCtsp(ctsp.getSanPham().getTen() + " - " + ctsp.getMauSac().getTen() + " / " + ctsp.getSize().getTen());
            gio.add(ct);
        }

        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }

    @PostMapping("/them-gio-hang")
    public String themVaoGioBangJson(@RequestBody Map<String, Object> payload,
                                     RedirectAttributes redirectAttributes,
                                     HttpSession session) {
        Integer id = Integer.parseInt(payload.get("idChiTietSp").toString());
        Integer soLuong = Integer.parseInt(payload.get("soLuong").toString());
        String cartKey = payload.get("cartKey").toString();

        System.out.println("=== DỮ LIỆU NHẬN ĐƯỢC ===");
        System.out.println("idChiTietSp: " + id);
        System.out.println("soLuong: " + soLuong);
        System.out.println("cartKey: " + cartKey);
        System.out.println("==========================");

        ChiTietSanPham ctsp = chiTietSanPhamRepository.findWithDetailsById(id);
        if (ctsp == null) {
            ThongBaoUtils.addError(redirectAttributes, "Không tìm thấy sản phẩm");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        List<HoaDonChiTiet> gio = getCart(cartKey, session);

        Optional<HoaDonChiTiet> existingItem = gio.stream()
                .filter(i -> i.getChiTietSanPham().getId().equals(id))
                .findFirst();

        int tongSoLuongMuonThem = soLuong + existingItem.map(HoaDonChiTiet::getSoLuong).orElse(0);
        if (tongSoLuongMuonThem > ctsp.getSoLuong()) {
            ThongBaoUtils.addError(redirectAttributes, "Sản phẩm không đủ tồn kho");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        if (existingItem.isPresent()) {
            existingItem.get().setSoLuong(tongSoLuongMuonThem);
        } else {
            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setChiTietSanPham(ctsp);
            ct.setSoLuong(soLuong);
            ct.setGiaGiam(BigDecimal.ZERO);
            ct.setGiaSauGiam(ctsp.getGiaBan());
            ct.setTrangThai(true);
            ct.setNgayTao(LocalDateTime.now());
            ct.setNguoiTao(getNhanVien.getCurrentNhanVien().getId());
            ct.setTenCtsp(ctsp.getSanPham().getTen() + " - " + ctsp.getMauSac().getTen() + " / " + ctsp.getSize().getTen());
            gio.add(ct);
        }

        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }

    @PostMapping("/xoa-san-pham")
    public String xoaSanPhamTrongGio(@RequestParam("cartKey") String cartKey,
                                     @RequestParam("idChiTietSp") Integer idChiTietSp,
                                     RedirectAttributes redirectAttributes,
                                     HttpSession session) {
        // Lấy giỏ hàng từ session
        List<HoaDonChiTiet> gio = getCart(cartKey, session);
        gio.removeIf(item -> item.getChiTietSanPham().getId().equals(idChiTietSp));

        ThongBaoUtils.addSuccess(redirectAttributes, "Xóa sản phẩm thành công");

        // Kiểm tra lại mã giảm giá
        Map<String, Object> cartInfo = (Map<String, Object>) session.getAttribute("cartInfo_" + cartKey);
        if (cartInfo != null && cartInfo.containsKey("maGiamGia")) {
            String maGiamGia = (String) cartInfo.get("maGiamGia");
            PhieuGiamGia phieu = phieuGiamGiaService.findByMa(maGiamGia);

            boolean hopLe = true;

            if (phieu != null) {
                // Nếu giỏ rỗng => không hợp lệ
                if (gio.isEmpty()) {
                    hopLe = false;
                } else {
                    // Tính lại tổng tiền giỏ hàng
                    BigDecimal tongTien = gio.stream()
                            .map(item -> item.getGiaSauGiam().multiply(BigDecimal.valueOf(item.getSoLuong())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Điều kiện: tổng tiền >= giá trị tối thiểu
                    if (tongTien.compareTo(phieu.getDieuKien()) < 0) {
                        hopLe = false;
                    }

                    // TODO: check thêm các điều kiện khác (danh mục, số lượng sp, ngày hết hạn, ...)
                }
            } else {
                hopLe = false; // mã không tồn tại
            }

            // Nếu không hợp lệ thì xóa mã đã add
            // Nếu không hợp lệ thì xóa mã đã add
            if (!hopLe) {
                cartInfo.remove("maGiamGia");
                cartInfo.remove("giaTriGiam");
                session.setAttribute("cartInfo_" + cartKey, cartInfo);

                // Xóa đúng attribute của giỏ hiện tại
                session.removeAttribute("maGiamGia_" + cartKey);
                session.setAttribute("giamGia_" + cartKey, BigDecimal.ZERO);

                ThongBaoUtils.addError(redirectAttributes,
                        "Mã giảm giá đã bị hủy do giỏ hàng không còn đủ điều kiện.");
            }

        }

        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }



    @PostMapping("/thanh-toan")
    public String thanhToan(@RequestParam("cartKey") String cartKey,
                            @RequestParam(value = "soDienThoai", required = false) String sdt,
                            @RequestParam(value = "soDienThoaivc", required = false) String sdtvc,
                            @RequestParam(value = "ten", required = false) String ten,
                            @RequestParam(value = "ghichu", required = false) String ghichu,
                            @RequestParam(value = "giagiam", required = false) BigDecimal giagiam,
                            @RequestParam(value = "magiam", required = false) String magiam,
                            @RequestParam("phuongThucThanhToan") String phuongThuc,
                            @RequestParam("diaChiTinh") String diaChiTinh,
                            @RequestParam("diaChiHuyen") String diaChiHuyen,
                            @RequestParam("diaChiXa") String diaChiXa,
                            HttpServletRequest request,
                            HttpSession session,
                            RedirectAttributes redirect) {

        List<HoaDonChiTiet> gio = getCart(cartKey, session);
        if (gio == null || gio.isEmpty()) {
            ThongBaoUtils.addError(redirect, "Giỏ hàng trống!");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        BigDecimal tongTien = gio.stream()
                .map(ct -> ct.getChiTietSanPham().getGiaBan().multiply(BigDecimal.valueOf(ct.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal phiShip = Optional.ofNullable((BigDecimal) session.getAttribute("phiVanChuyen")).orElse(BigDecimal.ZERO);
        giagiam = giagiam != null ? giagiam : BigDecimal.ZERO;
        BigDecimal thanhTien = tongTien.subtract(giagiam).add(phiShip);

        HoaDon hd = new HoaDon();
        String maHD = "HD" + System.currentTimeMillis();
        hd.setMa(maHD);
        hd.setNgayTao(LocalDateTime.now());
        hd.setNgayThanhToan(LocalDateTime.now());
        hd.setTenNguoiNhan("Trực tiếp");
        hd.setPhieuGiamGia(phieugiamgiarepository.findByMa(magiam));
        KhachHang kh = khachHangRepository.findSoDienThoai(sdt);
        if (kh != null) {
            hd.setKhachHang(kh);
        } else {
            KhachHang khachLe = khachHangRepository.findBySoDienThoai("000000000")
                    .orElse(null);
            hd.setKhachHang(khachLe);
        }
        NhanVien nv = getNhanVien.getCurrentNhanVien();
        if (nv == null) {
            ThongBaoUtils.addError(redirect, "Không tìm thấy nhân viên đang đăng nhập.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }
        hd.setNhanVien(nv);
        hd.setPhuongThuc(phuongThuc.equals("chuyen_khoan") ? "Chuyển khoản" : "Tiền mặt");
        System.out.println("=== DỮ LIỆU NHẬN ĐƯỢC ===");
        System.out.println("dc: " + diaChiTinh);
        System.out.println("dc: " + diaChiHuyen);
        System.out.println("dc: " + diaChiXa);
        System.out.println("==========================");


        String diachi= diaChiTinh + "-" + diaChiHuyen + "-" + diaChiXa;

        String tenn=ten+"/"+sdtvc;
        System.out.println(phiShip);
        System.out.println(BigDecimal.ZERO.equals(phiShip));
        if (!BigDecimal.ZERO.equals(phiShip)) {
            hd.setDiaChi(diachi);
            hd.setTenNguoiNhan(tenn);

            String ghiChuFull = "Đơn hàng vận chuyển. \nSố điện thoại người nhận:" + sdtvc;
            if (ghichu != null && !ghichu.trim().isEmpty()) {
                ghiChuFull += "\n" + ghichu;
            }

            hd.setGhiChu(ghiChuFull);
        }

        hd.setGiaGoc(tongTien);
        hd.setGiaGiamGia(giagiam);
        hd.setPhiVanChuyen(phiShip);
        hd.setThanhTien(thanhTien);
        hd.setTrangThai(3);
        hd.setNgaySua(LocalDateTime.now());
        hd.setNguoiSua(1);
        hd.setNguoiTao(1);
        hd.setLoaihoadon("Trực tiếp");
        session.setAttribute("hoaDonTam", hd);
        session.setAttribute("gioTam", gio);
        session.setAttribute("cartKeyTam", cartKey);

        return hoanTatThanhToan(cartKey, gio, sdt,sdtvc,tenn,ghichu, giagiam,magiam, tongTien, phiShip, phuongThuc,diachi,redirect, session);
    }

    private String hoanTatThanhToan(String cartKey, List<HoaDonChiTiet> gio,
                                    String sdt,String sdtvc,String ten,String ghichu, BigDecimal giagiam,String magiam, BigDecimal tongTien,
                                    BigDecimal phiShip, String phuongThuc,String diachi,
                                    RedirectAttributes redirectAttributes,
                                    HttpSession session) {

        HoaDon hd = new HoaDon();
        hd.setMa("HD" + System.currentTimeMillis());
        hd.setNgayTao(LocalDateTime.now());
        hd.setNgayThanhToan(LocalDateTime.now());

        NhanVien nv = getNhanVien.getCurrentNhanVien();
        if (nv == null) nv = nhanVienRepository.findById(1).orElse(null); // fallback nếu cần
        hd.setNhanVien(nv);

        hd.setDiaChi(diachi);
        KhachHang kh = khachHangRepository.findSoDienThoai(sdt);
        String tenn = ten + "/" + sdtvc;
//        System.out.println(!BigDecimal.ZERO.equals(phiShip));
        if (!BigDecimal.ZERO.equals(phiShip)) {
            hd.setDiaChi(diachi);
            hd.setTenNguoiNhan(tenn);

            String ghiChuFull = "Số điện thoại người nhận:" + sdtvc;
            if (ghichu != null && !ghichu.trim().isEmpty()) {
                ghiChuFull += "\n" + ghichu;
            }

            hd.setGhiChu(ghiChuFull);
        } else {
            hd.setTenNguoiNhan("Khách lẻ");
        }


        if (kh != null) {
            hd.setKhachHang(kh);
            hd.setSoDienThoai(sdt);
        } else {
            KhachHang khachLe = khachHangRepository.findBySoDienThoai("000000000")
                    .orElse(null);
            hd.setKhachHang(khachLe);
            hd.setSoDienThoai("Khách lẻ");
        }

        hd.setPhuongThuc(phuongThuc);
//        System.out.println(phuongThuc.equals("tien_mat") && !BigDecimal.ZERO.equals(phiShip));
        if (phuongThuc.equals("tien_mat") && !BigDecimal.ZERO.equals(phiShip)) {
            hd.setTrangThai(2);
        } else {
            hd.setTrangThai(3);
        }
        hd.setGiaGoc(tongTien);
        hd.setGiaGiamGia(giagiam);
        hd.setPhiVanChuyen(phiShip);
        hd.setLoaihoadon("Trực tiếp");
        hd.setThanhTien(tongTien.subtract(giagiam).add(phiShip));
        hd.setNgaySua(LocalDateTime.now());
        hd.setNgayThanhToan(LocalDateTime.now());
        hd.setNguoiTao(getNhanVien.getCurrentNhanVien().getId());
        // 👉 Lưu mã giảm giá nếu có
        hd.setPhieuGiamGia(phieugiamgiarepository.findByMa(magiam));

        hoaDonRepository.save(hd);

        for (HoaDonChiTiet ct : gio) {
            ChiTietSanPham sp = chiTietSanPhamRepository.findWithDetailsById(ct.getChiTietSanPham().getId());
            ct.setChiTietSanPham(sp);
            ct.setHoaDon(hd);
            ct.setGiaGoc(sp.getGiaBan());
            ct.setTenCtsp(sp.getSanPham().getTen() + " - " + sp.getMauSac().getTen() + "/" + sp.getSize().getTen());
            hoaDonChiTietRepository.save(ct);

            sp.setSoLuong(sp.getSoLuong() - ct.getSoLuong());
            chiTietSanPhamRepository.save(sp);
        }

        // ✅ Lưu lịch sử hóa đơn
        // Lấy từ session
        LichSuHoaDon lichSu1 = new LichSuHoaDon();
        LichSuHoaDon lichSu2 = new LichSuHoaDon();
        LichSuHoaDon lichSu3 = new LichSuHoaDon();
        lichSu1.setHoaDon(hd);
        lichSu1.setNgayTao(LocalDateTime.now());
        lichSu1.setNgaySua(LocalDateTime.now());
        lichSu1.setNguoiTao(nv.getId());
//        lichSu1.setNguoiSua(nv.getId());
        lichSu1.setGhiChu("Tạo hóa đơn: Chờ xác nhận");
        lichSu1.setTrangThai(1);

        lichSu2.setHoaDon(hd);
        lichSu2.setNgayTao(LocalDateTime.now());
        lichSu2.setNgaySua(LocalDateTime.now());
        lichSu2.setNguoiTao(nv.getId());
//        lichSu2.setNguoiSua(nv.getId());
        lichSu2.setGhiChu("Thay đổi trạng thái: Xác nhận");
        lichSu2.setTrangThai(2);

        lichSuHoaDonRepository.save(lichSu1);
        lichSuHoaDonRepository.save(lichSu2);

        if (BigDecimal.ZERO.equals(phiShip)) {
            lichSu3.setHoaDon(hd);
            lichSu3.setNgayTao(LocalDateTime.now());
            lichSu3.setNgaySua(LocalDateTime.now());
            lichSu3.setNguoiTao(nv.getId());
//            lichSu3.setNguoiSua(nv.getId());
            lichSu3.setGhiChu("Thanh toán" + (phuongThuc.equals("chuyen_khoan") ? " bằng chuyển khoản" : " bằng tiền mặt"));
            lichSu3.setTrangThai(5);

            lichSuHoaDonRepository.save(lichSu3);
        }

        if (magiam != null) {
            PhieuGiamGia phieu = phieugiamgiarepository.findByMa(magiam);
            if (phieu != null) {
                hd.setPhieuGiamGia(phieu);

                // ✅ Trừ số lượng phiếu
                if (phieu.getSoLuongTon() != null && phieu.getSoLuongTon() > 0) {
                    phieu.setSoLuongTon(phieu.getSoLuongTon() - 1);
                    phieugiamgiarepository.save(phieu);
                }
            }
        }

        // ✅ Dọn session
        Map<String, List<HoaDonChiTiet>> carts = (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
        if (carts != null) carts.remove(cartKey);
        session.removeAttribute("phiVanChuyen");
        session.removeAttribute("giamGia");
        session.removeAttribute("maGiamGia");
        session.removeAttribute("hoaDonTam");
        session.removeAttribute("gioTam");

        ThongBaoUtils.addSuccess(redirectAttributes, "Thanh toán thành công");

        return "redirect:/admin/ban-hang";
//        return "redirect:/admin/ban-hang/" + hd.getMa() + "/pdf";
    }

    @GetMapping("/dia-chi/tinh")
    @ResponseBody
    public List<Map<String, Object>> getTinh() {
        return ghnService.getProvinces();
    }

    @GetMapping("/dia-chi/huyen")
    @ResponseBody
    public List<Map<String, Object>> getHuyen(@RequestParam("provinceId") int provinceId) {
        return ghnService.getDistricts(provinceId);
    }

    @GetMapping("/dia-chi/xa")
    @ResponseBody
    public List<Map<String, Object>> getXa(@RequestParam("districtId") int districtId) {
        return ghnService.getWards(districtId);
    }

    @GetMapping("/phi-ship")
    @ResponseBody
    public ResponseEntity<Integer> getPhiShip(
            @RequestParam("toDistrictId") int toDistrictId,
            @RequestParam("wardCode") String toWardCode,
            @RequestParam("cartKey") String cartKey,
            HttpSession session) {

        int fromDistrictId = 3440; // Quận 1 mặc định

        // TODO: Tính khối lượng từ giỏ hàng thay vì fix 1000
        int weight = 1000;

        List<Map<String, Object>> services = ghnService.getAvailableServices(fromDistrictId, toDistrictId);
        if (services.isEmpty()) {
            session.setAttribute("phiVanChuyen_" + cartKey, BigDecimal.ZERO);
            return ResponseEntity.ok(0);
        }

        Object serviceIdObj = services.get(0).get("service_id");
        int serviceId = Integer.parseInt(serviceIdObj.toString());

        Integer fee = ghnService.getShippingFee(fromDistrictId, toDistrictId, toWardCode, weight, serviceId);

        BigDecimal phiShip = (fee != null && fee >= 0) ? BigDecimal.valueOf(fee) : BigDecimal.ZERO;

        session.setAttribute("phiVanChuyen", phiShip);
        session.setAttribute("phiVanChuyen_" + cartKey, phiShip);

        return ResponseEntity.ok(phiShip.intValue());
    }


    @PostMapping("/luu-dia-chi")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> luuDiaChiVaPhiShip(
            @RequestParam("cartKey") String cartKey,
            @RequestParam("tinh") String tinh,
            @RequestParam("huyen") String huyen,
            @RequestParam("xa") String xa,
            @RequestParam("toDistrictId") int toDistrictId,
            @RequestParam("wardCode") String toWardCode,
            HttpSession session) {

        int fromDistrictId = 3440;
        int weight = 1000;
        List<Map<String, Object>> services = ghnService.getAvailableServices(fromDistrictId, toDistrictId);
        if (services.isEmpty()) return ResponseEntity.ok(Map.of("phiShip", 0));

        int serviceId = (int) services.get(0).get("service_id");
        Integer fee = ghnService.getShippingFee(fromDistrictId, toDistrictId, toWardCode, weight, serviceId);

        // Lưu vào session
        session.setAttribute("phiVanChuyen", new BigDecimal(fee));
        session.setAttribute("diaChiTinh", tinh);
        session.setAttribute("diaChiHuyen", huyen);
        session.setAttribute("diaChiXa", xa);

        return ResponseEntity.ok(Map.of("phiShip", fee));
    }

//    @GetMapping("/{maHoaDon}/pdf")
//    public void xuatHoaDonPDF(@PathVariable("maHoaDon") String maHoaDon,
//                              HttpServletResponse response) throws Exception {
//        HoaDonDTO hoaDon = hoaDonService.getHoaDonByMa(maHoaDon);
//        List<HoaDonChiTietDTO> chiTietList = hoaDonChiTietService.getHoaDonChiTietByMaHoaDon(maHoaDon);
//
//        response.setContentType("application/pdf");
//        response.setHeader("Content-Disposition", "attachment; filename=hoa-don-" + maHoaDon + ".pdf");
//
//        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
//        PdfWriter.getInstance(document, response.getOutputStream());
//        document.open();
//
//
//        document.close();
//    }

//    @GetMapping("/{maHoaDon}/pdf/view")
//    public void xemHoaDonPDF(@PathVariable("maHoaDon") String maHoaDon,
//                             HttpServletResponse response) throws Exception {
//        xuatHoaDonPdfCommon(maHoaDon, response, false);
//    }
//
//    @GetMapping("/{maHoaDon}/pdf/download")
//    public void taiHoaDonPDF(@PathVariable("maHoaDon") String maHoaDon,
//                             HttpServletResponse response) throws Exception {
//        xuatHoaDonPdfCommon(maHoaDon, response, true);
//    }
//
//    private void xuatHoaDonPdfCommon(String maHoaDon, HttpServletResponse response, boolean download) throws Exception {
//        HoaDonDTO hoaDon = hoaDonService.getHoaDonByMa(maHoaDon);
//        List<HoaDonChiTietDTO> chiTietList = hoaDonChiTietService.getHoaDonChiTietByMaHoaDon(maHoaDon);
//
//        response.setContentType("application/pdf");
//        if (download) {
//            response.setHeader("Content-Disposition", "attachment; filename=hoa-don-" + maHoaDon + ".pdf");
//        } else {
//            response.setHeader("Content-Disposition", "inline; filename=hoa-don-" + maHoaDon + ".pdf");
//        }
//
//        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
//        PdfWriter.getInstance(document, response.getOutputStream());
//        document.open();
//
//        // Font cơ bản hỗ trợ tiếng Việt
//        BaseFont baseFont = BaseFont.createFont("fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
//        Font normalFont = new Font(baseFont, 12);
//        Font boldFont = new Font(baseFont, 14, Font.BOLD);
//
//        document.add(new Paragraph("HÓA ĐƠN BÁN HÀNG", boldFont));
//        document.add(new Paragraph("Mã hóa đơn: " + hoaDon.getMa(), normalFont));
//        document.add(new Paragraph("Khách hàng: " + hoaDon.getTenKH(), normalFont));
//        document.add(new Paragraph("Email: " + (hoaDon.getEmail() == null ? " " : hoaDon.getEmail() ), normalFont));
//        document.add(new Paragraph("Số điện thoại: " + (hoaDon.getSoDienThoai().equals("Khách lẻ") ? " " : hoaDon.getSoDienThoai() ), normalFont));
//        document.add(new Paragraph("Địa chỉ: " + (hoaDon.getDiaChi().equals("-- Chọn tỉnh ----") ? " " : hoaDon.getDiaChi()), normalFont));
//        document.add(new Paragraph("Ngày tạo: " + hoaDon.getNgayTao(), normalFont));
//        document.add(new Paragraph(" "));
//
//        PdfPTable table = new PdfPTable(5);
//        table.setWidthPercentage(100);
//        table.setSpacingBefore(10);
//        table.setWidths(new float[]{1f, 3f, 2f, 1f, 2f});
//
//        // Tiêu đề bảng
//        String[] headers = {"STT", "Tên sản phẩm", "Đơn giá", "SL", "Tổng"};
//        for (String h : headers) {
//            PdfPCell cell = new PdfPCell(new Phrase(h, boldFont));
//            table.addCell(cell);
//        }
//
//        int stt = 1;
//        for (HoaDonChiTietDTO ct : chiTietList) {
//            table.addCell(new Phrase(String.valueOf(stt++), normalFont));
//            table.addCell(new Phrase(ct.getTenCTSP(), normalFont));
//            table.addCell(new Phrase(String.valueOf(ct.getGiaSauGiam()), normalFont));
//            table.addCell(new Phrase(String.valueOf(ct.getSoLuong()), normalFont));
//            table.addCell(new Phrase(String.valueOf(ct.getTongTien()), normalFont));
//        }
//
//        document.add(table);
//        document.add(new Paragraph(" ", normalFont));
//
//        document.add(new Paragraph("Tổng tiền: " + hoaDon.getGiaGoc(), boldFont));
//        document.add(new Paragraph("Giá giảm: " + hoaDon.getGiaGiamGia(), boldFont));
//        document.add(new Paragraph("Phí vận chuyển: " + hoaDon.getPhiVanChuyen(), boldFont));
//        document.add(new Paragraph("Thành tiền: " + hoaDon.getThanhTien(), boldFont));
//
//        document.add(new Paragraph(" ", normalFont));
//
//        document.add(new Paragraph("Thanh toán: " + (hoaDon.getTrangThaiHoaDonString()), boldFont));
//
//
//        document.close();
//    }

}
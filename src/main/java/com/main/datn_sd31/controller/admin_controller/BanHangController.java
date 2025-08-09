package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.*;
import com.main.datn_sd31.repository.*;
import com.main.datn_sd31.service.PhieuGiamGiaService;
import com.main.datn_sd31.service.impl.GHNService;
import com.main.datn_sd31.util.GetNhanVien;
import com.main.datn_sd31.util.ThongBaoUtils;
import jakarta.servlet.http.HttpServletRequest;
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


    private List<HoaDonChiTiet> getCart(String cartKey, HttpSession session) {
        Map<String, List<HoaDonChiTiet>> carts = (Map<String, List<HoaDonChiTiet>>) session.getAttribute("tatCaGio");
        if (carts == null) {
            carts = new HashMap<>();
            session.setAttribute("tatCaGio", carts);
        }
        return carts.computeIfAbsent(cartKey, k -> new ArrayList<>());
    }

    @GetMapping
    public String hienThiSanPham(@RequestParam(value = "idSanPham", required = false) Integer idSanPham,
                                 @RequestParam(value = "cartKey", defaultValue = "gio-1") String cartKey,
                                 Model model, HttpSession session) {

        // Danh sách sản phẩm
        List<SanPham> dsSanPham = sanphamrepository.findAll();
        List<ChiTietSanPham> dsChiTiet = idSanPham != null
                ? chiTietSanPhamRepository.findBySanPhamId(idSanPham)
                : new ArrayList<>();

        // Lấy giỏ hàng theo cartKey
        List<HoaDonChiTiet> gio = getCart(cartKey, session);

        // Tính tổng tiền
        BigDecimal tongTien = gio.stream()
                .map(i -> i.getChiTietSanPham().getGiaBan().multiply(BigDecimal.valueOf(i.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Lấy phí ship và tiền giảm từ session
        BigDecimal phiShip = Optional.ofNullable((BigDecimal) session.getAttribute("phiVanChuyen")).orElse(BigDecimal.ZERO);
        BigDecimal giamGia = Optional.ofNullable((BigDecimal) session.getAttribute("giamGia")).orElse(BigDecimal.ZERO);

        // Tính tổng sau giảm
        BigDecimal tongSauGiam = tongTien.subtract(giamGia).add(phiShip);

        // Đẩy dữ liệu ra model
        model.addAttribute("dsSanPham", dsSanPham);
        model.addAttribute("dsChiTietSanPham", dsChiTiet);
        model.addAttribute("sanPhamDaChon", idSanPham);
        model.addAttribute("gioHang", gio);
        model.addAttribute("tatCaGio", ((Map<String, ?>) session.getAttribute("tatCaGio")).keySet());
        model.addAttribute("cartKey", cartKey);
        model.addAttribute("tongTien", tongTien);
        model.addAttribute("giamGia", giamGia);
        model.addAttribute("tongTienSauGiam", tongSauGiam);
        model.addAttribute("phiVanChuyen", phiShip);
        model.addAttribute("dsPhieuGiamGia", phieuGiamGiaService.findAllStatusTrue());
        model.addAttribute("maGiamGia", session.getAttribute("maGiamGia"));
        model.addAttribute("giamGia", session.getAttribute("giamGia"));
        return "admin/pages/banhang/banhang";
    }


    @PostMapping("/ap-dung-ma")
    public String apDungMa(@RequestParam("maGiamGia") String ma,
                           @RequestParam("cartKey") String cartKey,
                           HttpSession session) {
        List<HoaDonChiTiet> gio = getCart(cartKey, session);
        BigDecimal tongTien = gio.stream()
                .map(i -> i.getChiTietSanPham().getGiaBan().multiply(BigDecimal.valueOf(i.getSoLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PhieuGiamGia phieu = phieugiamgiarepository.findByMa(ma.trim());
        BigDecimal tienGiam = BigDecimal.ZERO;

        if (phieu != null && Boolean.TRUE.equals(phieu.getTrangThai())) {
            LocalDate today = LocalDate.now();
            if (phieu.getLoaiPhieuGiamGia() == 2) {
                tienGiam = phieu.getMucDo(); // giảm cố định
            } else if (phieu.getLoaiPhieuGiamGia() == 1) {
                tienGiam = tongTien.multiply(phieu.getMucDo()).divide(BigDecimal.valueOf(100));
                // Dùng giá trị giamToiDa từ DB nếu có
                if (phieu.getGiamToiDa() != null && phieu.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
                    tienGiam = tienGiam.min(phieu.getGiamToiDa());
                }
            }

        }

        session.setAttribute("giamGia", tienGiam);
        session.setAttribute("maGiamGia", ma.trim());
        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }

    @GetMapping("/xoa-ma")
    public String xoaMa(@RequestParam("cartKey") String cartKey, HttpSession session) {
        session.removeAttribute("giamGia");
        session.removeAttribute("maGiamGia");
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

    @GetMapping("/tim-kiem-san-pham")
    @ResponseBody
    public List<Map<String, Object>> timKiemSanPham(@RequestParam("keyword") String keyword) {
        List<ChiTietSanPham> danhSach = chiTietSanPhamRepository.findBySanPham_TenContainingIgnoreCase(keyword);

        List<Map<String, Object>> ketQua = new ArrayList<>();

        for (ChiTietSanPham ctsp : danhSach) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", ctsp.getId()); // ID của ChiTietSanPham
            map.put("ma", ctsp.getSanPham().getMa());
            map.put("tenSanPham", ctsp.getSanPham().getTen());

            // Hình ảnh đại diện (lấy cái đầu tiên nếu có)
            String hinhAnh = ctsp.getSanPham().getHinhAnhs().stream()
                    .findFirst()
                    .map(h -> h.getUrl())
                    .orElse("/images/no-image.png");
            map.put("hinhAnh", hinhAnh);

            // Thông tin màu, size và tồn kho
            map.put("mauSac", ctsp.getMauSac().getTen());
            map.put("kichThuoc", ctsp.getSize().getTen());
            map.put("soLuong", ctsp.getSoLuong());

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

        if (soLuong > ctsp.getSoLuong()) {
            redirectAttributes.addFlashAttribute("error", "❌ Số lượng vượt quá tồn kho.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }

        List<HoaDonChiTiet> gio = getCart(cartKey, session);
        gio.stream()
                .filter(item -> item.getChiTietSanPham().getId().equals(id))
                .findFirst()
                .ifPresent(item -> item.setSoLuong(soLuong));

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
                                     HttpSession session) {
        List<HoaDonChiTiet> gio = getCart(cartKey, session);
        gio.removeIf(item -> item.getChiTietSanPham().getId().equals(idChiTietSp));
        return "redirect:/admin/ban-hang?cartKey=" + cartKey;
    }


    @PostMapping("/thanh-toan")
    public String thanhToan(@RequestParam("cartKey") String cartKey,
                            @RequestParam(value = "soDienThoai", required = false) String sdt,
                            @RequestParam(value = "giagiam", required = false) BigDecimal giagiam,
                            @RequestParam("phuongThucThanhToan") String phuongThuc,
                            @RequestParam("diaChiTinh") String diaChiTinh,
                            @RequestParam("diaChiHuyen") String diaChiHuyen,
                            @RequestParam("diaChiXa") String diaChiXa,
                            HttpServletRequest request,
                            HttpSession session,
                            RedirectAttributes redirect) {

        List<HoaDonChiTiet> gio = getCart(cartKey, session);
        if (gio == null || gio.isEmpty()) {
            redirect.addFlashAttribute("error", "Giỏ hàng trống!");
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
        hd.setKhachHang(khachHangRepository.findById(1).orElse(null));
        NhanVien nv = getNhanVien.getCurrentNhanVien();
        if (nv == null) {
            redirect.addFlashAttribute("error", "Không tìm thấy nhân viên đang đăng nhập.");
            return "redirect:/admin/ban-hang?cartKey=" + cartKey;
        }
        hd.setNhanVien(nv);
        hd.setPhuongThuc(phuongThuc.equals("chuyen_khoan") ? "Chuyển khoản" : "Tiền mặt");
        String diachi;
        if (diaChiTinh == null || diaChiHuyen == null || diaChiXa == null) {
            diachi=diaChiTinh+'-'+diaChiHuyen+'-'+diaChiXa;
        } else {
            diachi=null;
        }
        hd.setDiaChi(diachi);
        hd.setSoDienThoai(sdt);
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

        return hoanTatThanhToan(cartKey, gio, sdt, giagiam, tongTien, phiShip, phuongThuc,diachi,redirect, session);
    }

    private String hoanTatThanhToan(String cartKey, List<HoaDonChiTiet> gio,
                                    String sdt, BigDecimal giagiam, BigDecimal tongTien,
                                    BigDecimal phiShip, String phuongThuc,String diachi,
                                    RedirectAttributes redirectAttributes,
                                    HttpSession session) {

        HoaDon hd = new HoaDon();
        hd.setMa("HD" + System.currentTimeMillis());
        hd.setNgayTao(LocalDateTime.now());
        hd.setNgayThanhToan(LocalDateTime.now());
        hd.setTenNguoiNhan("Trực tiếp");
        hd.setKhachHang(khachHangRepository.findById(1).orElse(null));
        NhanVien nv = getNhanVien.getCurrentNhanVien();
        if (nv == null) nv = nhanVienRepository.findById(1).orElse(null); // fallback nếu cần
        hd.setNhanVien(nv);

        hd.setTrangThai(3);

        hd.setDiaChi(diachi);
        hd.setPhuongThuc(phuongThuc);
        hd.setSoDienThoai(sdt);
        hd.setGiaGoc(tongTien);
        hd.setGiaGiamGia(giagiam);
        hd.setPhiVanChuyen(phiShip);
        hd.setLoaihoadon("Trực tiếp");
        hd.setThanhTien(tongTien.subtract(giagiam).add(phiShip));
        hd.setNgaySua(LocalDateTime.now());
        hd.setNgayThanhToan(LocalDateTime.now());
        hd.setNguoiTao(getNhanVien.getCurrentNhanVien().getId());
        // 👉 Lưu mã giảm giá nếu có
        String maGiamGia = (String) session.getAttribute("maGiamGia");
        if (maGiamGia != null) {
            hd.setPhieuGiamGia(phieugiamgiarepository.findByMa(maGiamGia));
        }

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

        if (diachi == null) {
            lichSu3.setHoaDon(hd);
            lichSu3.setNgayTao(LocalDateTime.now());
            lichSu3.setNgaySua(LocalDateTime.now());
            lichSu3.setNguoiTao(nv.getId());
//            lichSu3.setNguoiSua(nv.getId());
            lichSu3.setGhiChu("Thanh toán" + (phuongThuc.equals("chuyen_khoan") ? " bằng chuyển khoản" : " bằng tiền mặt"));
            lichSu3.setTrangThai(5);

            lichSuHoaDonRepository.save(lichSu3);
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
            HttpSession session) {
        int fromDistrictId = 3440; // Mặc định Quận 1
        int weight = 1000;
        List<Map<String, Object>> services = ghnService.getAvailableServices(fromDistrictId, toDistrictId);
        if (services.isEmpty()) return ResponseEntity.ok(0);

        int serviceId = (int) services.get(0).get("service_id");
        Integer fee = ghnService.getShippingFee(fromDistrictId, toDistrictId, toWardCode, weight, serviceId);
        System.out.println("idChiTietSp: " + fee);
        session.setAttribute("phiVanChuyen", new BigDecimal(fee));
        return ResponseEntity.ok(fee);
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

}
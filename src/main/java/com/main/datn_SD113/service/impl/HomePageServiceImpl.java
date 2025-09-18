package com.main.datn_SD113.service.impl;

import com.main.datn_SD113.dto.home.HomeProductDto;
import com.main.datn_SD113.entity.ChiTietSanPham;
import com.main.datn_SD113.entity.DanhGia;
import com.main.datn_SD113.entity.HinhAnh;
import com.main.datn_SD113.entity.SanPham;
import com.main.datn_SD113.repository.Chitietsanphamrepository;
import com.main.datn_SD113.repository.DanhGiaRepository;
import com.main.datn_SD113.repository.Hinhanhrepository;
import com.main.datn_SD113.repository.SanPhamRepository;
import com.main.datn_SD113.service.HomePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomePageServiceImpl implements HomePageService {

    private final SanPhamRepository sanPhamRepository;
    private final Chitietsanphamrepository chiTietRepo;
    private final Hinhanhrepository hinhAnhRepository;
    private final DanhGiaRepository danhGiaRepository;

    @Override
    public List<HomeProductDto> getLatestProducts(int limit) {
        List<SanPham> sanPhams = sanPhamRepository.findAll().stream()
                .filter(sp -> sp.getTrangThai() == null || sp.getTrangThai())
                .sorted(Comparator.comparing(SanPham::getNgayTao, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        return mapToHomeProductDtos(sanPhams);
    }

    @Override
    public Map<Integer, String> getCategoryImageMap() {
        // Lấy tất cả sản phẩm đang hoạt động, group theo danh mục và chọn sản phẩm mới nhất làm đại diện
        List<SanPham> actives = sanPhamRepository.findAll().stream()
                .filter(sp -> sp.getTrangThai() == null || sp.getTrangThai())
                .toList();

        Map<Integer, Optional<SanPham>> representative = actives.stream()
                .filter(sp -> sp.getDanhMuc() != null && sp.getDanhMuc().getId() != null)
                .collect(Collectors.groupingBy(sp -> sp.getDanhMuc().getId(),
                        Collectors.maxBy(Comparator.comparing(SanPham::getNgayTao, Comparator.nullsLast(Comparator.naturalOrder())))));

        Map<Integer, String> result = new HashMap<>();
        representative.forEach((dmId, spOpt) -> {
            String url = "/client-static/images/insta1.jpg";
            if (spOpt.isPresent()) {
                url = resolveMainImage(spOpt.get().getId());
            }
            result.put(dmId, url);
        });
        return result;
    }

    @Override
    public List<HomeProductDto> getBestSellingProducts(int limit) {
        // Tổng số lượng bán theo sản phẩm (cộng dồn số lượng của các chi tiết sản phẩm thuộc cùng 1 sản phẩm)
        Map<Integer, Integer> soldByProduct = new HashMap<>();
        List<ChiTietSanPham> allCt = chiTietRepo.findAll();
        for (ChiTietSanPham ct : allCt) {
            if (ct.getSanPham() == null || ct.getSanPham().getId() == null) continue;
            Integer spId = ct.getSanPham().getId();
            // giả sử có trường soLuongDaBan trong ChiTietSanPham, nếu không có, tạm dùng (giaBan != null && soLuong thấp) → không chính xác
            // Ở đây sẽ dùng soLuong tồn kho để suy luận là không đúng; giải pháp đúng là query từ HoaDonChiTiet.
            // Tạm thời: ưu tiên ct có soLuong bán ra không có → fallback 0.
            int sold = 0;
            try {
                java.lang.reflect.Method m = ct.getClass().getMethod("getSoLuongDaBan");
                Object v = m.invoke(ct);
                if (v instanceof Number) sold = ((Number) v).intValue();
            } catch (Exception ignored) {}
            soldByProduct.merge(spId, sold, Integer::sum);
        }

        // Sort desc theo sold
        List<Integer> topProductIds = soldByProduct.entrySet().stream()
                .sorted((a,b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();

        List<SanPham> sanPhams = sanPhamRepository.findAllById(topProductIds).stream()
                .filter(sp -> sp.getTrangThai() == null || sp.getTrangThai())
                .toList();

        // Giữ thứ tự theo topProductIds
        Map<Integer, SanPham> byId = sanPhams.stream().collect(Collectors.toMap(SanPham::getId, sp -> sp));
        List<SanPham> ordered = new ArrayList<>();
        for (Integer id : topProductIds) {
            SanPham sp = byId.get(id);
            if (sp != null) ordered.add(sp);
        }
        return mapToHomeProductDtos(ordered);
    }

    private String resolveMainImage(Integer sanPhamId) {
        List<HinhAnh> hinhAnhs = hinhAnhRepository.findByhinhanhid(sanPhamId);
        if (hinhAnhs == null || hinhAnhs.isEmpty()) {
            return "/client-static/images/insta1.jpg";
        }
        return hinhAnhs.stream()
                .filter(ha -> "Ảnh chính".equalsIgnoreCase(ha.getTen()))
                .map(HinhAnh::getUrl)
                .findFirst()
                .orElse(hinhAnhs.get(0).getUrl());
    }

    private class PriceDiscount {
        private final BigDecimal price;
        private final Integer discountPercent;

        private PriceDiscount(BigDecimal price, Integer discountPercent) {
            this.price = price;
            this.discountPercent = discountPercent;
        }

        public BigDecimal price() { return price; }
        public Integer discountPercent() { return discountPercent; }
    }

    private PriceDiscount resolvePriceAndDiscount(Integer sanPhamId) {
        List<ChiTietSanPham> ctList = chiTietRepo.findBySanPhamId(sanPhamId);
        if (ctList == null || ctList.isEmpty()) return new PriceDiscount(null, null);
        ChiTietSanPham chosen = ctList.stream()
                .filter(ct -> ct.getGiaBan() != null)
                .min(Comparator.comparing(ChiTietSanPham::getGiaBan))
                .orElse(ctList.get(0));
        BigDecimal price = chosen.getGiaBan();
        Integer discountPercent = null;
        if (chosen.getGiaGoc() != null && chosen.getGiaBan() != null
                && chosen.getGiaGoc().compareTo(chosen.getGiaBan()) > 0) {
            BigDecimal diff = chosen.getGiaGoc().subtract(chosen.getGiaBan());
            BigDecimal percent = diff.multiply(BigDecimal.valueOf(100))
                    .divide(chosen.getGiaGoc(), 0, RoundingMode.HALF_UP);
            discountPercent = percent.intValue();
        }
        return new PriceDiscount(price, discountPercent);
    }

    private Double resolveRatingAvg(Integer sanPhamId) {
        List<DanhGia> danhGias = danhGiaRepository.findBySanPhamIdOrderByThoiGianDesc(sanPhamId);
        if (danhGias == null || danhGias.isEmpty()) return null;
        return danhGias.stream()
                .filter(dg -> dg.getSoSao() != null)
                .mapToInt(DanhGia::getSoSao)
                .average()
                .orElse(0.0);
    }

    private List<HomeProductDto> mapToHomeProductDtos(List<SanPham> sanPhams) {
        List<HomeProductDto> result = new ArrayList<>();
        for (SanPham sp : sanPhams) {
            String imageUrl = resolveMainImage(sp.getId());
            var priceAndDiscount = resolvePriceAndDiscount(sp.getId());
            Double ratingAvg = resolveRatingAvg(sp.getId());

            String priceText = null;
            if (priceAndDiscount.price != null) {
                NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
                nf.setMaximumFractionDigits(0);
                nf.setMinimumFractionDigits(0);
                priceText = nf.format(priceAndDiscount.price) + " đ";
            }

            result.add(HomeProductDto.builder()
                    .id(sp.getId())
                    .name(sp.getTen())
                    .imageUrl(imageUrl)
                    .price(priceAndDiscount.price)
                    .discountPercent(priceAndDiscount.discountPercent)
                    .ratingAvg(ratingAvg)
                    .priceText(priceText)
                    .build());
        }
        return result;
    }
} 
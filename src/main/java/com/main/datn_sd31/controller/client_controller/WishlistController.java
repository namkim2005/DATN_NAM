package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.SanPham;
import com.main.datn_sd31.entity.SpYeuThich;
import com.main.datn_sd31.repository.SanPhamRepository;
import com.main.datn_sd31.repository.SpYeuThichRepository;
import com.main.datn_sd31.util.GetKhachHang;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/yeu-thich")
public class WishlistController {
	private final SpYeuThichRepository spYeuThichRepository;
	private final SanPhamRepository sanPhamRepository;
	private final GetKhachHang getKhachHang;

	@PostMapping("/toggle")
	public ResponseEntity<?> toggleWishlist(@RequestBody Map<String, Integer> body) {
		Integer productId = body.get("productId");
		KhachHang current = getKhachHang.getCurrentKhachHang();
		if (current == null) {
			return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
		}
		if (productId == null) {
			return ResponseEntity.badRequest().body(Map.of("message", "Thiếu productId"));
		}
		boolean exists = spYeuThichRepository.existsBySanPham_IdAndKhachHang_Id(productId, current.getId());
		boolean liked;
		if (exists) {
			spYeuThichRepository.findBySanPham_IdAndKhachHang_Id(productId, current.getId())
				.ifPresent(spYeuThichRepository::delete);
			liked = false;
		} else {
			SanPham sp = sanPhamRepository.findById(productId).orElse(null);
			if (sp == null) {
				return ResponseEntity.badRequest().body(Map.of("message", "Sản phẩm không tồn tại"));
			}
			SpYeuThich s = new SpYeuThich();
			s.setSanPham(sp);
			s.setKhachHang(current);
			s.setThoiGianThem(Instant.now());
			spYeuThichRepository.save(s);
			liked = true;
		}
		return ResponseEntity.ok(Map.of("liked", liked));
	}
} 
package com.main.datn_sd31.security;

import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.repository.NhanVienRepository;
import com.main.datn_sd31.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Đang đăng nhập với email: " + email);

        /* ==== 1. Thử tìm nhân viên ==== */
        NhanVien nv = nhanVienRepository.findByEmail(email).orElse(null);
        if (nv != null) {
            System.out.println("Tìm thấy nhân viên, mật khẩu (BCrypt): " + nv.getMatKhau());
            return buildUser(
                    nv.getEmail(),
                    nv.getMatKhau(),
                    "ROLE_ADMIN"      // hoặc nv.getRole().getName() nếu bạn có bảng Role
            );
        }

        /* ==== 2. Thử tìm khách hàng ==== */
        KhachHang kh = khachHangRepository.findByEmail(email).orElse(null);
        if (kh != null) {
            System.out.println("Tìm thấy khách hàng, mật khẩu (BCrypt): " + kh.getMatKhau());
            return buildUser(
                    kh.getEmail(),
                    kh.getMatKhau(),
                    "ROLE_CUSTOMER"
            );
        }

        /* ==== 3. Không tìm thấy ==== */
        throw new UsernameNotFoundException("Không tìm thấy tài khoản");
    }

    private UserDetails buildUser(String email, String password, String role) {
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(email)
                .password(password)
                .authorities(new SimpleGrantedAuthority(role))
                .build();
    }


}
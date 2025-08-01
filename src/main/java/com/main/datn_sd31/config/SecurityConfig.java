package com.main.datn_sd31.config;

import com.main.datn_sd31.security.CustomAuthenticationFailureHandler;
import com.main.datn_sd31.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /* ===== 1. Bảo vệ khu vực ADMIN ===== */
    @Bean
    @Order(1)           // Ưu tiên cao nhất
    public SecurityFilterChain adminSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")      // Chỉ áp dụng filter‑chain này cho /admin/**
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().hasAnyRole("ADMIN", "NHANVIEN")
                )
                .formLogin(form -> form
                        .loginPage("/admin/dang-nhap")
                        .loginProcessingUrl("/admin/dang-nhap") // phải khớp với form
                        .defaultSuccessUrl("/admin", true)
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/dang-nhap?logout=true")
                        .permitAll()

                );
        return http.build();
    }

    /* ===== 2. Bảo vệ khu vực KHÁCH HÀNG ===== */
    @Order(2)
    @Bean
    public SecurityFilterChain customerSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/khach-hang/**", "/gio-hang/**", "/khach-hang/dang-nhap")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/khach-hang/dang-nhap",
                                "/khach-hang/dang-ky",
                                "/khach-hang/danh-sach",
                                "/khach-hang/chi-tiet/**",
                                "/khach-hang/quen-mat-khau",
                                "/khach-hang/public/**"
                        ).permitAll()
                        .anyRequest().hasRole("KHACHHANG")
                )
                .formLogin(form -> form
                        .loginPage("/khach-hang/dang-nhap")
                        .loginProcessingUrl("/khach-hang/dang-nhap")
                        .defaultSuccessUrl("/san-pham/danh-sach", true)
                        .permitAll()
                )
                .logout(lg -> lg
                        .logoutUrl("/khach-hang/dang-xuat")
                        .logoutSuccessUrl("/san-pham/danh-sach")
                        .invalidateHttpSession(true)                  // ✅ XÓA SESSION
                        .deleteCookies("JSESSIONID")                  // ✅ XÓA COOKIE phiên
                        .clearAuthentication(true)                    // ✅ XÓA thông tin xác thực
                );

        return http.build();
    }
    /* ===== 3. Cấu hình chung cho phần công khai (static, trang chủ, v.v.) ===== */
    @Bean
    @Order(3)           // Thấp nhất
    public SecurityFilterChain publicSecurity(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/uploads/**",
                                "/css/**", "/js/**", "/images/**",
                                "/vendors/**", "/webjars/**",
                                "/static/**", "/favicon.ico",
                                "/san-pham/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/error"); // 👉 Trang bạn tự tạo
                        })
                )
                .logout(lg -> lg.logoutSuccessUrl("/"))
                .formLogin(AbstractHttpConfigurer::disable); // Vô hiệu hóa login mặc định
        return http.build();
    }

//    /* ===== Bean chung ===== */
//    @Bean
//    public UserDetailsService userDetailsService() {
//        return new CustomUserDetailsService();      // Phải trả về ROLE_ADMIN hoặc ROLE_CUSTOMER tương ứng
//    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(authProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
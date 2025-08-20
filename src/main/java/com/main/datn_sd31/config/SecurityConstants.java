package com.main.datn_sd31.config;

/**
 * Security Constants - Centralized URL patterns and security configurations
 */
public final class SecurityConstants {
    
    // ==================== URL PATTERNS ====================
    
    // Admin URLs
    public static final String ADMIN_BASE = "/admin";
    public static final String ADMIN_LOGIN = "/admin/dang-nhap";
    public static final String ADMIN_LOGOUT = "/admin/logout";
    public static final String ADMIN_DASHBOARD = "/admin/thong-ke";
    
    // Customer URLs
    public static final String CUSTOMER_BASE = "/khach-hang";
    public static final String CUSTOMER_LOGIN = "/khach-hang/dang-nhap";
    public static final String CUSTOMER_LOGOUT = "/khach-hang/dang-xuat";
    public static final String CUSTOMER_REGISTER = "/khach-hang/dang-ky";
    public static final String CUSTOMER_FORGOT_PASSWORD = "/khach-hang/quen-mat-khau";
    
    // Auth API URLs
    public static final String AUTH_BASE = "/auth";
    public static final String AUTH_STATUS = "/auth/status";
    public static final String AUTH_ADMIN_LOGOUT = "/auth/admin/logout";
    public static final String AUTH_CUSTOMER_LOGOUT = "/auth/customer/logout";
    
    // Public URLs
    public static final String HOME = "/";
    public static final String PRODUCTS = "/san-pham";
    public static final String CART = "/gio-hang";
    
    // ==================== ROLES ====================
    
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_NHANVIEN = "ROLE_NHANVIEN";
    public static final String ROLE_KHACHHANG = "ROLE_KHACHHANG";
    
    // ==================== SECURITY MATCHERS ====================
    
    public static final String[] ADMIN_PATTERNS = {"/admin/**"};
    public static final String[] CUSTOMER_PATTERNS = {"/khach-hang/**", "/gio-hang/**"};
    public static final String[] PUBLIC_PATTERNS = {
        "/", "/home", "/login",
        "/uploads/**", "/css/**", "/js/**", "/images/**",
        "/vendors/**", "/webjars/**", "/static/**", "/favicon.ico",
        "/san-pham/**", "/client-static/**", "/bootstrap-5.3.7-dist/**",
        "/api/auth/**"
    };
    
    public static final String[] ADMIN_PERMIT_ALL = {
        ADMIN_LOGIN, ADMIN_LOGOUT, "/admin/san-pham/api/export-excel"
    };
    
    public static final String[] CUSTOMER_PERMIT_ALL = {
        CUSTOMER_LOGIN, CUSTOMER_REGISTER, CUSTOMER_FORGOT_PASSWORD,
        "/khach-hang/danh-sach", "/khach-hang/chi-tiet/**",
        "/khach-hang/public/**", CUSTOMER_LOGOUT, "/api/auth/logout"
    };
    
    public static final String[] CUSTOMER_PROTECTED = {
        "/khach-hang/thong-tin", "/khach-hang/don-hang",
        "/khach-hang/yeu-thich", "/khach-hang/dia-chi"
    };
    
    // ==================== CSRF EXEMPTIONS ====================
    
    public static final String[] CSRF_EXEMPTIONS = {
        "/admin/san-pham/tao-ma-ngau-nhien",
        "/admin/san-pham/api/export-excel"
    };
    
    // Private constructor to prevent instantiation
    private SecurityConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
} 
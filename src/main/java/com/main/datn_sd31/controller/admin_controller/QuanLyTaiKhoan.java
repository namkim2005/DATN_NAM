package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.entity.KhachHang;
import com.main.datn_sd31.entity.NhanVien;
import com.main.datn_sd31.repository.KhachHangRepository;
import com.main.datn_sd31.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/quan-ly-tai-khoan")
public class QuanLyTaiKhoan {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @GetMapping
    public String index(Model model, @RequestParam(defaultValue = "nhanvien") String activeTab) {
        model.addAttribute("nhanvienList", nhanVienRepository.findAll());
        model.addAttribute("khachhangList", khachHangRepository.findAll());
        model.addAttribute("nhanvien", new NhanVien());
        model.addAttribute("khachhang", new KhachHang());
        model.addAttribute("activeTab", activeTab);
        return "admin/pages/quan-ly-tai-khoan/quan-ly-tai-khoan";
    }

    // ---------------------- Nhân viên ----------------------

    @PostMapping("/save-nhan-vien")
    public String saveNhanVien(@ModelAttribute("nhanvien") NhanVien nhanVien) {
        nhanVienRepository.save(nhanVien);
        return "redirect:/admin/quan-ly-tai-khoan?activeTab=nhanvien";
    }

    @GetMapping("/edit-nhan-vien")
    public String editNhanVien(@RequestParam("id") Integer id, Model model) {
        model.addAttribute("nhanvienList", nhanVienRepository.findAll());
        model.addAttribute("khachhangList", khachHangRepository.findAll());
        model.addAttribute("nhanvien", nhanVienRepository.findById(id).orElse(new NhanVien()));
        model.addAttribute("khachhang", new KhachHang());
        model.addAttribute("activeTab", "nhanvien");
        return "admin/pages/quan-ly-tai-khoan/quan-ly-tai-khoan";
    }

    @GetMapping("/delete-nhan-vien")
    public String deleteNhanVien(@RequestParam("id") Integer id) {
        nhanVienRepository.deleteById(id);
        return "redirect:/admin/quan-ly-tai-khoan?activeTab=nhanvien";
    }

    @GetMapping("/search-nhan-vien")
    public String searchNhanVien(@RequestParam("id") Integer id, Model model) {
        NhanVien nv = nhanVienRepository.findById(id).orElse(null);
        model.addAttribute("nhanvienList", nv != null ? List.of(nv) : List.of());
        model.addAttribute("khachhangList", khachHangRepository.findAll());
        model.addAttribute("nhanvien", nv != null ? nv : new NhanVien());
        model.addAttribute("khachhang", new KhachHang());
        model.addAttribute("activeTab", "nhanvien");
        return "admin/pages/quan-ly-tai-khoan/quan-ly-tai-khoan";
    }

    // ---------------------- Khách hàng ----------------------

    @PostMapping("/save-khach-hang")
    public String saveKhachHang(@ModelAttribute("khachhang") KhachHang khachHang) {
        khachHangRepository.save(khachHang);
        return "redirect:/admin/quan-ly-tai-khoan?activeTab=khachhang";
    }

    @GetMapping("/edit-khach-hang")
    public String editKhachHang(@RequestParam("id") Long id, Model model) {
        model.addAttribute("nhanvienList", nhanVienRepository.findAll());
        model.addAttribute("khachhangList", khachHangRepository.findAll());
        model.addAttribute("nhanvien", new NhanVien());
        model.addAttribute("khachhang", khachHangRepository.findById(id).orElse(new KhachHang()));
        model.addAttribute("activeTab", "khachhang");
        return "admin/pages/quan-ly-tai-khoan/quan-ly-tai-khoan";
    }

    @GetMapping("/delete-khach-hang")
    public String deleteKhachHang(@RequestParam("id") Long id) {
        khachHangRepository.deleteById(id);
        return "redirect:/admin/quan-ly-tai-khoan?activeTab=khachhang";
    }

    @GetMapping("/search-khach-hang")
    public String searchKhachHang(@RequestParam("id") Long id, Model model) {
        KhachHang kh = khachHangRepository.findById(id).orElse(null);
        model.addAttribute("nhanvienList", nhanVienRepository.findAll());
        model.addAttribute("khachhangList", kh != null ? List.of(kh) : List.of());
        model.addAttribute("nhanvien", new NhanVien());
        model.addAttribute("khachhang", kh != null ? kh : new KhachHang());
        model.addAttribute("activeTab", "khachhang");
        return "admin/pages/quan-ly-tai-khoan/quan-ly-tai-khoan";
    }
}
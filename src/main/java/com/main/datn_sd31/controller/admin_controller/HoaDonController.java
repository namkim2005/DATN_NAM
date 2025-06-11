package com.main.datn_sd31.controller.admin_controller;

import com.main.datn_sd31.Enum.TrangThaiLichSuHoaDon;
import com.main.datn_sd31.dto.hoa_don_dto.HoaDonDTO;
import com.main.datn_sd31.service.HoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/hoa-don")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonService hoaDonService;

    @GetMapping("")
    public String hoaDon(Model model) {
        model.addAttribute("page", "admin/pages/hoa-don");
        model.addAttribute("hoaDonList", hoaDonService.getAllHoaDon());
        model.addAttribute("trangThaiCount", hoaDonService.getTrangThaiCount());
        return "admin/index";
    }

    @GetMapping("/cho-xac-nhan")
    public String choXacNhan(Model model) {
        model.addAttribute("page", "admin/pages/hoa-don");
        model.addAttribute("hoaDonList", hoaDonService.getAllHoaDonByStatus(TrangThaiLichSuHoaDon.CHO_XAC_NHAN.getMoTa()));
        model.addAttribute("trangThaiCount", hoaDonService.getTrangThaiCount());
        return "admin/index";
    }

    @GetMapping("/xac-nhan")
    public String xacNhan(Model model) {
        model.addAttribute("page", "admin/pages/hoa-don");
        model.addAttribute("hoaDonList", hoaDonService.getAllHoaDonByStatus(TrangThaiLichSuHoaDon.XAC_NHAN.getMoTa()));
        model.addAttribute("trangThaiCount", hoaDonService.getTrangThaiCount());
        return "admin/index";
    }

    @GetMapping("/cho-giao-hang")
    public String choGiaoHang(Model model) {
        model.addAttribute("page", "admin/pages/hoa-don");
        model.addAttribute("hoaDonList", hoaDonService.getAllHoaDonByStatus(TrangThaiLichSuHoaDon.CHO_GIAO_HANG.getMoTa()));
        model.addAttribute("trangThaiCount", hoaDonService.getTrangThaiCount());
        return "admin/index";
    }

    @GetMapping("/da-giao")
    public String daGiao(Model model) {
        model.addAttribute("page", "admin/pages/hoa-don");
        model.addAttribute("hoaDonList", hoaDonService.getAllHoaDonByStatus(TrangThaiLichSuHoaDon.DA_GIAO.getMoTa()));
        model.addAttribute("trangThaiCount", hoaDonService.getTrangThaiCount());
        return "admin/index";
    }

    @GetMapping("/hoan-thanh")
    public String hoanThanh(Model model) {
        model.addAttribute("page", "admin/pages/hoa-don");
        model.addAttribute("hoaDonList", hoaDonService.getAllHoaDonByStatus(TrangThaiLichSuHoaDon.HOAN_THANH.getMoTa()));
        model.addAttribute("trangThaiCount", hoaDonService.getTrangThaiCount());
        return "admin/index";
    }

    @GetMapping("/huy")
    public String huy(Model model) {
        model.addAttribute("page", "admin/pages/hoa-don");
        model.addAttribute("hoaDonList", hoaDonService.getAllHoaDonByStatus(TrangThaiLichSuHoaDon.HUY.getMoTa()));
        model.addAttribute("trangThaiCount", hoaDonService.getTrangThaiCount());
        return "admin/index";
    }

}

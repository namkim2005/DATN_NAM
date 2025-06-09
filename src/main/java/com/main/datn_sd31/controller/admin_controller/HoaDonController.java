package com.main.datn_sd31.controller.admin_controller;

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
        List<HoaDonDTO> hoaDonDTOList = hoaDonService.getAllHoaDon();
        model.addAttribute("page", "admin/pages/hoa-don");
        model.addAttribute("hoaDonList", hoaDonDTOList);
        Map<String, Long> trangThaiCount = hoaDonDTOList.stream()
                .collect(Collectors.groupingBy(HoaDonDTO::getTrangThai, Collectors.counting()));
        System.out.println(trangThaiCount);
        model.addAttribute("trangThaiCount", trangThaiCount);
        return "admin/index";
    }

}

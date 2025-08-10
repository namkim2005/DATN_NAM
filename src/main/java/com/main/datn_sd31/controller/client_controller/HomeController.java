package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.dto.home.HomeProductDto;
import com.main.datn_sd31.service.HomePageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HomePageService homePageService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("activePage", "home");
        List<HomeProductDto> products = homePageService.getLatestProducts(8);
        model.addAttribute("homeProducts", products);
        return "client/pages/home";
    }

    @GetMapping("/home")
    public String showHome(Model model) {
        model.addAttribute("activePage", "home");
        List<HomeProductDto> products = homePageService.getLatestProducts(8);
        model.addAttribute("homeProducts", products);
        return "client/pages/home";
    }
}

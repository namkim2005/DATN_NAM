package com.main.datn_sd31.controller.client_controller;

import com.main.datn_sd31.dto.home.HomeProductDto;
import com.main.datn_sd31.service.HomePageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    private final HomePageService homePageService;

    public HomeController(HomePageService homePageService) {
        this.homePageService = homePageService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<HomeProductDto> latest = homePageService.getLatestProducts(8);
        model.addAttribute("newProducts", latest);
        model.addAttribute("categoryImages", homePageService.getCategoryImageMap());
        model.addAttribute("bestSelling", homePageService.getBestSellingProducts(8));
        return "client/pages/home";
    }

    @GetMapping("/home")
    public String homePage(Model model, @RequestParam(value = "added", required = false) String added,
                           @RequestParam(value = "product", required = false) String productId) {
        if ("true".equals(added) && productId != null) {
            model.addAttribute("success", "Đã thêm sản phẩm vào giỏ hàng thành công!");
        }
        List<HomeProductDto> latest = homePageService.getLatestProducts(8);
        model.addAttribute("newProducts", latest);
        model.addAttribute("categoryImages", homePageService.getCategoryImageMap());
        model.addAttribute("bestSelling", homePageService.getBestSellingProducts(8));
        return "client/pages/home";
    }
}

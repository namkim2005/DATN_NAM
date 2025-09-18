package com.main.datn_SD113.controller.client_controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class InfoController {

    @GetMapping("/gioi-thieu")
    public String gioiThieu(Model model) {
        model.addAttribute("activePage", "about");
        return "client/pages/about";
    }

    @GetMapping("/lien-he")
    public String lienHe(Model model) {
        model.addAttribute("activePage", "contact");
        return "client/pages/contact";
    }

    @PostMapping("/lien-he/gui")
    public String guiLienHe(@RequestParam("name") String name,
                           @RequestParam("email") String email,
                           @RequestParam("phone") String phone,
                           @RequestParam("message") String message,
                           RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("success", "Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi sớm nhất.");
        return "redirect:/lien-he";
    }
}

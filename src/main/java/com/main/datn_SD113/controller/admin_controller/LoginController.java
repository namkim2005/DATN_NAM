package com.main.datn_SD113.controller.admin_controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/admin/dang-nhap")
    public String loginPage(){
        return "admin/login";
    }

}

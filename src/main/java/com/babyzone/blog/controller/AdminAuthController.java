package com.babyzone.blog.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.babyzone.blog.interceptor.AdminAuthInterceptor;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("pageTitle", "后台登录");
        return "admin/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          Model model,
                          HttpSession session) {
        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            session.setAttribute(AdminAuthInterceptor.ADMIN_AUTH_SESSION_KEY, true);
            return "redirect:/admin";
        }
        model.addAttribute("error", "用户名或密码错误");
        model.addAttribute("pageTitle", "后台登录");
        return "admin/login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
}

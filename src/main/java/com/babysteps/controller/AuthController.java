package com.babysteps.controller;

import com.babysteps.dto.ApiMessageResponse;
import com.babysteps.dto.AuthStatusResponse;
import com.babysteps.dto.LoginRequest;
import com.babysteps.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthStatusResponse login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        return authService.login(request.username(), request.password(), session);
    }

    @GetMapping("/me")
    public AuthStatusResponse me(HttpSession session) {
        return authService.me(session);
    }

    @PostMapping("/logout")
    public ApiMessageResponse logout(HttpSession session) {
        authService.logout(session);
        return new ApiMessageResponse("已退出登录。");
    }
}

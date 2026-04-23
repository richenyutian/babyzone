package com.babysteps.service;

import com.babysteps.config.AppDataProperties;
import com.babysteps.dto.AuthStatusResponse;
import com.babysteps.model.Settings;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final SettingsService settingsService;
    private final PasswordEncoder passwordEncoder;
    private final AppDataProperties properties;

    public AuthService(SettingsService settingsService, PasswordEncoder passwordEncoder, AppDataProperties properties) {
        this.settingsService = settingsService;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    public AuthStatusResponse login(String username, String password, HttpSession session) {
        Settings settings = settingsService.getSettings();
        boolean valid = settings.adminUsername().equals(username)
                && passwordEncoder.matches(password, settings.adminPasswordHash());
        if (!valid) {
            throw new IllegalArgumentException("账号或密码不正确。");
        }
        session.setAttribute(properties.getSessionAttribute(), true);
        session.setAttribute("ADMIN_USERNAME", settings.adminUsername());
        return new AuthStatusResponse(true, settings.adminUsername());
    }

    public AuthStatusResponse me(HttpSession session) {
        if (session == null) {
            return new AuthStatusResponse(false, null);
        }
        boolean authenticated = Boolean.TRUE.equals(session.getAttribute(properties.getSessionAttribute()));
        String username = authenticated ? (String) session.getAttribute("ADMIN_USERNAME") : null;
        return new AuthStatusResponse(authenticated, username);
    }

    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }
}

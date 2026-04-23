package com.babysteps.service;

import java.time.LocalDateTime;

import com.babysteps.dto.SettingsResponse;
import com.babysteps.model.AppConfig;
import com.babysteps.model.Settings;
import com.babysteps.repository.SettingsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    private final SettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;

    public SettingsService(SettingsRepository settingsRepository, PasswordEncoder passwordEncoder) {
        this.settingsRepository = settingsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void initialize(AppConfig config) {
        if (settingsRepository.find().isEmpty()) {
            settingsRepository.insert(
                    config.babyName(),
                    config.babyBirthday(),
                    config.adminUsername(),
                    passwordEncoder.encode(config.adminPassword()),
                    LocalDateTime.now()
            );
        }
    }

    public Settings getSettings() {
        return settingsRepository.find().orElseThrow(() -> new IllegalStateException("系统尚未初始化设置。"));
    }

    public SettingsResponse getSettingsResponse() {
        Settings settings = getSettings();
        return new SettingsResponse(settings.babyName(), settings.babyBirthday().toString(), settings.adminUsername());
    }
}

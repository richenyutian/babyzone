package com.babysteps.service;

import com.babysteps.model.AppConfig;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupInitializer implements ApplicationRunner {

    private final ConfigFileService configFileService;
    private final SettingsService settingsService;

    public StartupInitializer(ConfigFileService configFileService, SettingsService settingsService) {
        this.configFileService = configFileService;
        this.settingsService = settingsService;
    }

    @Override
    public void run(ApplicationArguments args) {
        AppConfig config = configFileService.loadOrCreate();
        settingsService.initialize(config);
    }
}

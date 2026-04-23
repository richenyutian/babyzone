package com.babysteps.service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Properties;

import com.babysteps.config.AppDataProperties;
import com.babysteps.model.AppConfig;
import org.springframework.stereotype.Service;

@Service
public class ConfigFileService {

    private final AppDataProperties properties;

    public ConfigFileService(AppDataProperties properties) {
        this.properties = properties;
    }

    public AppConfig loadOrCreate() {
        try {
            Files.createDirectories(properties.dataRootPath());
            Files.createDirectories(properties.configDirectoryPath());
            Files.createDirectories(properties.uploadsPath());
            Path configFile = properties.configFilePath();
            if (Files.notExists(configFile)) {
                createDefaultConfig(configFile);
            }
            return readConfig(configFile);
        } catch (IOException exception) {
            throw new IllegalStateException("初始化配置目录失败。", exception);
        }
    }

    private void createDefaultConfig(Path configFile) throws IOException {
        Properties defaults = new Properties();
        defaults.setProperty("baby.name", "Little Baby");
        defaults.setProperty("baby.birthday", LocalDate.now().toString());
        defaults.setProperty("admin.username", "admin");
        defaults.setProperty("admin.password", "ChangeMe123!");
        try (Writer writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
            defaults.store(writer, "Baby Steps initial configuration");
        }
    }

    private AppConfig readConfig(Path configFile) throws IOException {
        Properties values = new Properties();
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            values.load(reader);
        }
        return new AppConfig(
                require(values, "baby.name"),
                LocalDate.parse(require(values, "baby.birthday")),
                require(values, "admin.username"),
                require(values, "admin.password")
        );
    }

    private String require(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("配置文件缺少必填项: " + key);
        }
        return value.trim();
    }
}

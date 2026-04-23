package com.babysteps.config;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppDataProperties {

    private String dataRoot = "./data";
    private String uploadsDirectory = "uploads";
    private String configDirectory = "config";
    private String configFileName = "app-config.properties";
    private String sessionAttribute = "BABY_STEPS_AUTHENTICATED";

    public String getDataRoot() {
        return dataRoot;
    }

    public void setDataRoot(String dataRoot) {
        this.dataRoot = dataRoot;
    }

    public String getUploadsDirectory() {
        return uploadsDirectory;
    }

    public void setUploadsDirectory(String uploadsDirectory) {
        this.uploadsDirectory = uploadsDirectory;
    }

    public String getConfigDirectory() {
        return configDirectory;
    }

    public void setConfigDirectory(String configDirectory) {
        this.configDirectory = configDirectory;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public String getSessionAttribute() {
        return sessionAttribute;
    }

    public void setSessionAttribute(String sessionAttribute) {
        this.sessionAttribute = sessionAttribute;
    }

    public Path dataRootPath() {
        return Path.of(dataRoot).toAbsolutePath().normalize();
    }

    public Path uploadsPath() {
        return dataRootPath().resolve(uploadsDirectory);
    }

    public Path configDirectoryPath() {
        return dataRootPath().resolve(configDirectory);
    }

    public Path configFilePath() {
        return configDirectoryPath().resolve(configFileName);
    }
}

package com.babyzone.blog;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootApplication
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(BlogApplication.class);
        application.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) event ->
                prepareRuntimeDirectories(event.getEnvironment()));
        application.run(args);
    }

    private static void prepareRuntimeDirectories(ConfigurableEnvironment environment) {
        createDirectoryIfNeeded(environment.getProperty("upload.path", "./uploads"));
        String datasourceUrl = environment.getProperty("spring.datasource.url", "");
        Path sqliteDatabaseFile = extractSqliteFile(datasourceUrl);
        if (sqliteDatabaseFile != null && sqliteDatabaseFile.getParent() != null) {
            createDirectoryIfNeeded(sqliteDatabaseFile.getParent().toString());
        }
    }

    private static Path extractSqliteFile(String jdbcUrl) {
        String prefix = "jdbc:sqlite:";
        if (jdbcUrl == null || !jdbcUrl.startsWith(prefix)) {
            return null;
        }
        String location = jdbcUrl.substring(prefix.length());
        int queryIndex = location.indexOf('?');
        if (queryIndex >= 0) {
            location = location.substring(0, queryIndex);
        }
        if (location.isBlank() || ":memory:".equalsIgnoreCase(location)) {
            return null;
        }
        try {
            if (location.startsWith("file:")) {
                URI uri = URI.create(location);
                return Paths.get(uri);
            }
        } catch (Exception ignored) {
            // Fallback to plain path parsing.
        }
        return Paths.get(location);
    }

    private static void createDirectoryIfNeeded(String pathText) {
        if (pathText == null || pathText.isBlank()) {
            return;
        }
        Path path = Paths.get(pathText).toAbsolutePath().normalize();
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建目录: " + path, e);
        }
    }
}

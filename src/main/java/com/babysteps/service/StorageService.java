package com.babysteps.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.babysteps.config.AppDataProperties;
import com.babysteps.model.Photo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private final AppDataProperties properties;

    public StorageService(AppDataProperties properties) {
        this.properties = properties;
    }

    public List<Photo> storeAll(long recordId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        try {
            Files.createDirectories(properties.uploadsPath());
            List<Photo> saved = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null
                        ? "photo"
                        : file.getOriginalFilename());
                String storedFileName = UUID.randomUUID() + "-" + originalFileName.replaceAll("\\s+", "-");
                Path targetPath = properties.uploadsPath().resolve(storedFileName);
                try (var inputStream = file.getInputStream()) {
                    Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                saved.add(new Photo(0L, recordId, storedFileName, originalFileName));
            }
            return saved;
        } catch (IOException exception) {
            throw new IllegalStateException("保存照片失败。", exception);
        }
    }
}

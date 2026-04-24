package com.babysteps.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.babysteps.dto.HomeResponse;
import com.babysteps.dto.PhotoResponse;
import com.babysteps.dto.RecordResponse;
import com.babysteps.model.AlbumRecord;
import com.babysteps.model.Photo;
import com.babysteps.model.Settings;
import com.babysteps.repository.PhotoRepository;
import com.babysteps.repository.RecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RecordService {

    private final RecordRepository recordRepository;
    private final PhotoRepository photoRepository;
    private final StorageService storageService;
    private final SettingsService settingsService;
    private final AgeCalculator ageCalculator;
    private final MarkdownService markdownService;

    public RecordService(RecordRepository recordRepository, PhotoRepository photoRepository, StorageService storageService,
                         SettingsService settingsService, AgeCalculator ageCalculator, MarkdownService markdownService) {
        this.recordRepository = recordRepository;
        this.photoRepository = photoRepository;
        this.storageService = storageService;
        this.settingsService = settingsService;
        this.ageCalculator = ageCalculator;
        this.markdownService = markdownService;
    }

    public HomeResponse getHome() {
        Settings settings = settingsService.getSettings();
        List<AlbumRecord> records = recordRepository.findAllDescending();
        Map<Long, List<Photo>> photosByRecordId = photoRepository.findByRecordIds(
                records.stream().map(AlbumRecord::id).toList()
        );

        List<RecordResponse> timeline = records.stream()
                .map(record -> toResponse(record, photosByRecordId.getOrDefault(record.id(), List.of()), settings.babyBirthday()))
                .toList();

        LocalDate today = LocalDate.now();
        return new HomeResponse(
                settings.babyName(),
                settings.babyBirthday().toString(),
                ageCalculator.daysSinceBirth(settings.babyBirthday(), today),
                ageCalculator.formatAge(settings.babyBirthday(), today),
                timeline
        );
    }

    @Transactional
    public void createRecord(String content, LocalDate date, String tags, List<MultipartFile> files) {
        String normalizedContent = content == null ? "" : content.trim();
        if (normalizedContent.isBlank()) {
            throw new IllegalArgumentException("记录内容不能为空。");
        }
        LocalDate normalizedDate = date == null ? LocalDate.now() : date;
        String normalizedTags = tags == null ? null : tags.trim();

        long recordId = recordRepository.insert(normalizedContent, normalizedDate, normalizedTags, LocalDateTime.now());
        List<Photo> storedPhotos = storageService.storeAll(recordId, files);
        for (Photo storedPhoto : storedPhotos) {
            photoRepository.insert(recordId, storedPhoto.filePath(), storedPhoto.fileName());
        }
    }

    private RecordResponse toResponse(AlbumRecord record, List<Photo> photos, LocalDate birthDate) {
        String excerpt = buildExcerpt(record.content());
        List<String> tags = splitTags(record.tags());
        List<PhotoResponse> photoResponses = photos.stream()
                .map(photo -> new PhotoResponse(photo.fileName(), "/media/" + photo.filePath()))
                .toList();
        return new RecordResponse(
                record.id(),
                record.content(),
                excerpt,
                markdownService.renderHtml(record.content()),
                record.date().toString(),
                ageCalculator.formatAge(birthDate, record.date()),
                tags,
                photoResponses
        );
    }

    private String buildExcerpt(String content) {
        String normalized = content.replaceAll("[#>*_`\\n\\r]+", " ").replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 140) {
            return normalized;
        }
        return normalized.substring(0, 140) + "...";
    }

    private List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }
}

package com.babysteps.controller;

import java.time.LocalDate;
import java.util.List;

import com.babysteps.dto.ApiMessageResponse;
import com.babysteps.dto.SettingsResponse;
import com.babysteps.service.RecordService;
import com.babysteps.service.SettingsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RecordService recordService;
    private final SettingsService settingsService;

    public AdminController(RecordService recordService, SettingsService settingsService) {
        this.recordService = recordService;
        this.settingsService = settingsService;
    }

    @GetMapping("/settings")
    public SettingsResponse settings() {
        return settingsService.getSettingsResponse();
    }

    @PostMapping(value = "/records", consumes = "multipart/form-data")
    public ApiMessageResponse createRecord(
            @RequestParam String content,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false, name = "files") List<MultipartFile> files
    ) {
        recordService.createRecord(content, date, tags, files);
        return new ApiMessageResponse("记录已保存。");
    }
}

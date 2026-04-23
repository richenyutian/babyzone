package com.babysteps.controller;

import com.babysteps.dto.HomeResponse;
import com.babysteps.service.RecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final RecordService recordService;

    public PublicController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/home")
    public HomeResponse home() {
        return recordService.getHome();
    }
}

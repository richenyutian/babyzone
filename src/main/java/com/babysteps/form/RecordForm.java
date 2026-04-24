package com.babysteps.form;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

public class RecordForm {

    @NotBlank(message = "记录内容不能为空")
    private String content;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date = LocalDate.now();

    private String tags;

    public static RecordForm withToday() {
        RecordForm form = new RecordForm();
        form.setDate(LocalDate.now());
        return form;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}

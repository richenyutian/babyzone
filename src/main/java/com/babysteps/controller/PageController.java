package com.babysteps.controller;

import java.util.List;

import com.babysteps.form.LoginForm;
import com.babysteps.form.RecordForm;
import com.babysteps.service.AuthService;
import com.babysteps.service.MarkdownService;
import com.babysteps.service.RecordService;
import com.babysteps.service.SettingsService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
public class PageController {

    private final RecordService recordService;
    private final SettingsService settingsService;
    private final AuthService authService;
    private final MarkdownService markdownService;

    public PageController(RecordService recordService, SettingsService settingsService,
                          AuthService authService, MarkdownService markdownService) {
        this.recordService = recordService;
        this.settingsService = settingsService;
        this.authService = authService;
        this.markdownService = markdownService;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("home", recordService.getHome());
        model.addAttribute("authenticated", authService.me(session).authenticated());
        return "public/home";
    }

    @GetMapping("/admin/login")
    public String loginPage(Model model, HttpSession session) {
        if (authService.me(session).authenticated()) {
            return "redirect:/admin/records/new";
        }
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
                        BindingResult bindingResult,
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/login";
        }

        try {
            authService.login(loginForm.getUsername(), loginForm.getPassword(), session);
            redirectAttributes.addFlashAttribute("successMessage", "登录成功，可以开始发布成长记录了。");
            return "redirect:/admin/records/new";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return "admin/login";
        }
    }

    @PostMapping("/admin/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        authService.logout(session);
        redirectAttributes.addFlashAttribute("successMessage", "已退出后台登录。");
        return "redirect:/";
    }

    @GetMapping("/admin/records/new")
    public String newRecordPage(Model model) {
        prepareAdminModel(model);
        if (!model.containsAttribute("recordForm")) {
            model.addAttribute("recordForm", RecordForm.withToday());
        }
        return "admin/record-form";
    }

    @PostMapping(value = "/admin/records", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createRecord(@Valid @ModelAttribute("recordForm") RecordForm recordForm,
                               BindingResult bindingResult,
                               @RequestParam(name = "files", required = false) List<MultipartFile> files,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareAdminModel(model);
            model.addAttribute("errorMessage", "请完善内容后再提交。");
            return "admin/record-form";
        }

        try {
            recordService.createRecord(recordForm.getContent(), recordForm.getDate(), recordForm.getTags(), files);
            redirectAttributes.addFlashAttribute("successMessage", "记录已保存到时光轴。");
            return "redirect:/admin/records/new";
        } catch (IllegalArgumentException exception) {
            prepareAdminModel(model);
            model.addAttribute("errorMessage", exception.getMessage());
            return "admin/record-form";
        }
    }

    @PostMapping(value = "/admin/markdown/preview", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String previewMarkdown(@RequestParam(defaultValue = "") String content) {
        return markdownService.renderHtml(content);
    }

    private void prepareAdminModel(Model model) {
        model.addAttribute("settings", settingsService.getSettingsResponse());
        model.addAttribute("home", recordService.getHome());
    }
}

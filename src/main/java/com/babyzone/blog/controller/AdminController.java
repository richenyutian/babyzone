package com.babyzone.blog.controller;

import com.babyzone.blog.entity.Post;
import com.babyzone.blog.service.PostService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final PostService postService;
    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping
    public String adminIndex(Model model) {
        model.addAttribute("posts", postService.findAllDesc());
        return "admin/list";
    }

    @GetMapping("/new")
    public String newPost(Model model) {
        model.addAttribute("post", new Post());
        return "admin/form";
    }

    @PostMapping("/posts")
    public String createPost(@ModelAttribute Post post, RedirectAttributes redirectAttributes) {
        postService.createPost(post.getTitle(), post.getCategory(), post.getMarkdownContent());
        redirectAttributes.addFlashAttribute("success", "文章已发布");
        return "redirect:/admin";
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("code", 1, "msg", "文件为空"));
        }
        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String filename = Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + ext;
        Path target = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        String url = "/uploads/" + filename;
        return ResponseEntity.ok(Map.of(
                "code", 0,
                "msg", "上传成功",
                "data", Map.of("url", url)
        ));
    }
}

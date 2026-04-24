package com.babyzone.blog.controller;

import com.babyzone.blog.entity.Post;
import com.babyzone.blog.service.PostService;
import java.io.IOException;
import java.io.InputStream;
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
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public String adminIndex(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            Model model) {
        Page<Post> postPage = postService.findAdminPage(page, size, keyword);
        model.addAttribute("postPage", postPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);
        return "admin/list";
    }

    @GetMapping("/new")
    public String newPost(Model model) {
        model.addAttribute("post", new Post());
        model.addAttribute("isEdit", false);
        return "admin/form";
    }

    @PostMapping("/posts")
    public String createPost(@ModelAttribute Post post, RedirectAttributes redirectAttributes) {
        postService.createPost(post.getTitle(), post.getCategory(), post.getMarkdownContent());
        redirectAttributes.addFlashAttribute("success", "文章已发布");
        return "redirect:/admin";
    }

    @GetMapping("/posts/{id}/edit")
    public String editPostPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return postService.findById(id)
                .map(post -> {
                    model.addAttribute("post", post);
                    model.addAttribute("isEdit", true);
                    return "admin/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "文章不存在");
                    return "redirect:/admin";
                });
    }

    @PostMapping("/posts/{id}")
    public String updatePost(
            @PathVariable Long id,
            @ModelAttribute Post post,
            RedirectAttributes redirectAttributes) {
        boolean updated = postService.updatePost(id, post.getTitle(), post.getCategory(), post.getMarkdownContent());
        if (!updated) {
            redirectAttributes.addFlashAttribute("error", "文章不存在");
        } else {
            redirectAttributes.addFlashAttribute("success", "文章已更新");
        }
        return "redirect:/admin";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = postService.deleteById(id);
        if (!deleted) {
            redirectAttributes.addFlashAttribute("error", "文章不存在");
        } else {
            redirectAttributes.addFlashAttribute("success", "文章已删除");
        }
        return "redirect:/admin";
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("code", 1, "msg", "文件为空"));
        }
        try {
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);
            String ext = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String filename = Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + ext;
            Path target = uploadDir.resolve(filename);
            // 显式关闭上传输入流，避免 Windows 下临时文件句柄未释放。
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            String url = "/uploads/" + filename;
            return ResponseEntity.ok(Map.of(
                    "code", 0,
                    "msg", "上传成功",
                    "data", Map.of("url", url)
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("code", 1, "msg", "上传失败: " + e.getMessage()));
        }
    }
}

package com.babyzone.blog.controller;

import com.babyzone.blog.entity.Post;
import com.babyzone.blog.service.PostService;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class ViewController {

    private final PostService postService;

    public ViewController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Post> posts = postService.findAll();
        model.addAttribute("posts", posts);
        model.addAttribute("pageTitle", "首页");
        return "index";
    }

    @GetMapping("/category/{slug}")
    public String category(@PathVariable String slug, Model model) {
        List<Post> posts = postService.findByCategory(slug);
        model.addAttribute("posts", posts);
        model.addAttribute("categorySlug", slug);
        model.addAttribute("categoryLabel", categoryName(slug));
        model.addAttribute("pageTitle", categoryName(slug));
        return "category";
    }

    @GetMapping("/post/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Optional<Post> post = postService.findById(id);
        if (post.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("post", post.get());
        model.addAttribute("pageTitle", post.get().getTitle());
        return "post-detail";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "关于");
        return "about";
    }

    private String categoryName(String slug) {
        return switch (slug) {
            case "snippets" -> "片言只语";
            case "essays" -> "娓娓道来";
            case "notes" -> "记事成册";
            case "dev-log" -> "编程日志";
            default -> "分类";
        };
    }
}

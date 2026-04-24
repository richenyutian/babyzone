package com.babyzone.blog.service;

import com.babyzone.blog.entity.Post;
import com.babyzone.blog.repository.PostRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    @Value("${upload.path}")
    private String uploadPath;
    private final Parser markdownParser = Parser.builder().build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    public Page<Post> findPage(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), size);
        if (keyword == null || keyword.isBlank()) {
            return postRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return postRepository.searchAll(keyword.trim(), pageable);
    }

    public Page<Post> findCategoryPage(String category, int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), size);
        if (keyword == null || keyword.isBlank()) {
            return postRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
        }
        return postRepository.searchByCategory(category, keyword.trim(), pageable);
    }

    public Page<Post> findAdminPage(int page, int size, String keyword) {
        return findPage(page, size, keyword);
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    @Transactional
    public Post createPost(String title, String category, String markdownContent) {
        Node markdownNode = markdownParser.parse(markdownContent);
        String htmlContent = htmlRenderer.render(markdownNode);
        Post post = Post.builder()
                .title(title)
                .category(category)
                .categoryLabel(categoryLabel(category))
                .markdownContent(markdownContent)
                .content(htmlContent)
                .build();
        return postRepository.save(post);
    }

    @Transactional
    public boolean updatePost(Long id, String title, String category, String markdownContent) {
        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isEmpty()) {
            return false;
        }
        Node markdownNode = markdownParser.parse(markdownContent);
        String htmlContent = htmlRenderer.render(markdownNode);
        Post post = optionalPost.get();
        post.setTitle(title);
        post.setCategory(category);
        post.setCategoryLabel(categoryLabel(category));
        post.setMarkdownContent(markdownContent);
        post.setContent(htmlContent);
        postRepository.save(post);
        return true;
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (!postRepository.existsById(id)) {
            return false;
        }
        postRepository.deleteById(id);
        return true;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    private String categoryLabel(String category) {
        return switch (category) {
            case "snippets" -> "片言只语";
            case "essays" -> "娓娓道来";
            case "notes" -> "记事成册";
            case "dev-log" -> "编程日志";
            default -> category;
        };
    }
}

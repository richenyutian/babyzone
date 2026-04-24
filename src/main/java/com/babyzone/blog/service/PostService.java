package com.babyzone.blog.service;

import com.babyzone.blog.entity.Post;
import com.babyzone.blog.repository.PostRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Value;
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

    public List<Post> findAll() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Post> findAllDesc() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Post> findByCategory(String category) {
        return postRepository.findByCategoryOrderByCreatedAtDesc(category);
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

package com.babyzone.blog.repository;

import com.babyzone.blog.entity.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findAllByOrderByCreatedAtDesc();

    List<Post> findByCategoryOrderByCreatedAtDesc(String category);
}

package com.babyzone.blog.repository;

import com.babyzone.blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findByCategoryOrderByCreatedAtDesc(String category, Pageable pageable);

    @Query("""
            SELECT p
            FROM Post p
            WHERE lower(p.title) LIKE lower(concat('%', :keyword, '%'))
               OR lower(p.markdownContent) LIKE lower(concat('%', :keyword, '%'))
            ORDER BY p.createdAt DESC
            """)
    Page<Post> searchAll(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT p
            FROM Post p
            WHERE p.category = :category
              AND (lower(p.title) LIKE lower(concat('%', :keyword, '%'))
                OR lower(p.markdownContent) LIKE lower(concat('%', :keyword, '%')))
            ORDER BY p.createdAt DESC
            """)
    Page<Post> searchByCategory(
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable);
}

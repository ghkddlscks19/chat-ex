package org.ktb.chatexample.repository;

import org.ktb.chatexample.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 작성자로 게시글 검색
    List<Post> findByAuthor(String author);
    
    // 제목으로 게시글 검색 (부분 일치)
    List<Post> findByTitleContaining(String title);
    
    // 제목 또는 내용으로 게시글 검색 (부분 일치)
    List<Post> findByTitleContainingOrContentContaining(String title, String content);
    
    // 페이징 처리된 게시글 목록 조회
    Page<Post> findAll(Pageable pageable);
    
    // 작성자별 페이징 처리된 게시글 목록 조회
    Page<Post> findByAuthor(String author, Pageable pageable);


}
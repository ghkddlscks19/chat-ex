package org.ktb.chatexample.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ktb.chatexample.dto.PostDto;
import org.ktb.chatexample.dto.PostResponseDto;
import org.ktb.chatexample.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PostController {

    private final PostService postService;

    // 게시글 목록 조회 (페이징)
    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getPosts(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(postService.getPaginatedPosts(pageable));
    }

    // 게시글 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    // 게시글 작성
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@Valid @RequestBody PostDto postDto) {
        PostResponseDto createdPost = postService.createPost(postDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long id, 
            @Valid @RequestBody PostDto postDto) {
        return ResponseEntity.ok(postService.updatePost(id, postDto));
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    // 작성자로 게시글 검색
    @GetMapping("/search/author/{author}")
    public ResponseEntity<List<PostResponseDto>> searchByAuthor(@PathVariable String author) {
        return ResponseEntity.ok(postService.getPostsByAuthor(author));
    }

    // 키워드로 게시글 검색
    @GetMapping("/search")
    public ResponseEntity<List<PostResponseDto>> searchPosts(@RequestParam String keyword) {
        return ResponseEntity.ok(postService.getPostsByTitleOrContent(keyword));
    }
}
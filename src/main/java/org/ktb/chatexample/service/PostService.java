package org.ktb.chatexample.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.ktb.chatexample.dto.PostDto;
import org.ktb.chatexample.dto.PostResponseDto;
import org.ktb.chatexample.entity.Post;
import org.ktb.chatexample.repository.ChatRoomRepository;
import org.ktb.chatexample.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    
    // 모든 게시글 조회
    @Transactional(readOnly = true)
    public List<PostResponseDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    // 페이징된 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPaginatedPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(this::convertToResponseDto);
    }
    
    // 게시글 ID로 조회
    @Transactional(readOnly = true)
    public PostResponseDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " + id));
        return convertToResponseDto(post);
    }
    
    // 게시글 작성
    public PostResponseDto createPost(PostDto postDto) {
        Post post = Post.builder()
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .author(postDto.getAuthor())
                .build();
        
        Post savedPost = postRepository.save(post);
        return convertToResponseDto(savedPost);
    }
    
    // 게시글 수정
    public PostResponseDto updatePost(Long id, PostDto postDto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " + id));
        
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        
        Post updatedPost = postRepository.save(post);
        return convertToResponseDto(updatedPost);
    }
    
    // 게시글 삭제
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " + id);
        }
        postRepository.deleteById(id);
    }
    
    // 작성자로 게시글 검색
    @Transactional(readOnly = true)
    public List<PostResponseDto> getPostsByAuthor(String author) {
        return postRepository.findByAuthor(author).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    // 제목으로 게시글 검색
    @Transactional(readOnly = true)
    public List<PostResponseDto> getPostsByTitle(String title) {
        return postRepository.findByTitleContaining(title).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    // 제목 또는 내용으로 게시글 검색
    @Transactional(readOnly = true)
    public List<PostResponseDto> getPostsByTitleOrContent(String keyword) {
        return postRepository.findByTitleContainingOrContentContaining(keyword, keyword).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    // Post 엔티티를 PostResponseDto로 변환
    private PostResponseDto convertToResponseDto(Post post) {
        boolean hasChat = chatRoomRepository.existsByPost(post);
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .hasChat(hasChat)
                .build();
    }
}
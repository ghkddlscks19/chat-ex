package org.ktb.chatexample.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1; // 게시글 작성자
    
    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2; // 채팅 요청자
    
    @Column(nullable = false, unique = true)
    private String roomId; // WebSocket 세션 ID
    
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatMessage> messages = new ArrayList<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
package org.ktb.chatexample.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
    private Long id;
    private String roomId;
    private Long postId;
    private String postTitle;
    private Long user1Id; // 게시글 작성자
    private String user1Name;
    private Long user2Id; // 채팅 요청자
    private String user2Name;
    private LocalDateTime createdAt;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
}
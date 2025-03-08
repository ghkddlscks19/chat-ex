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
public class ChatMessageDto {
    private Long id;
    private String roomId;
    private Long senderId;
    private String senderName;
    private String content;
    private String imageUrl;
    private MessageType type;
    private LocalDateTime createdAt;
}
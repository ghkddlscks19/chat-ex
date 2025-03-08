package org.ktb.chatexample.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequestDto {
    @NotBlank(message = "채팅방 ID는 필수 입력 항목입니다.")
    private String roomId;

    @NotNull(message = "발신자 ID는 필수 입력 항목입니다.")
    private Long senderId;

    @NotBlank(message = "메시지 내용은 필수 입력 항목입니다.")
    private String content;

    private MessageType type = MessageType.CHAT;
}
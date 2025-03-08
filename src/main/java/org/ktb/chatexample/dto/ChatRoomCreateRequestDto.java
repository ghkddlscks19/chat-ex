package org.ktb.chatexample.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomCreateRequestDto {
    @NotNull(message = "게시글 ID는 필수 입력 항목입니다.")
    private Long postId;
    
    @NotNull(message = "요청자 ID는 필수 입력 항목입니다.")
    private Long requestUserId;
}
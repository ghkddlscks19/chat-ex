package org.ktb.chatexample.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private Long id;
    
    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    @Size(min = 2, max = 100, message = "제목은 2-100자 사이여야 합니다.")
    private String title;
    
    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    private String content;
    
    @NotBlank(message = "작성자는 필수 입력 항목입니다.")
    private String author;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
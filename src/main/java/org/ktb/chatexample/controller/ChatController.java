package org.ktb.chatexample.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ktb.chatexample.dto.*;
import org.ktb.chatexample.service.ChatService;
import org.ktb.chatexample.service.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChatController {

    private final ChatService chatService;
    private final S3Service s3Service;
    private final SimpMessagingTemplate messagingTemplate;

    // 채팅방 생성
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomDto> createChatRoom(
            @Valid @RequestBody ChatRoomCreateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.createChatRoom(requestDto));
    }

    // 사용자별 채팅방 목록 조회
    @GetMapping("/rooms/user/{userId}")
    public ResponseEntity<List<ChatRoomDto>> getUserChatRooms(@PathVariable Long userId) {
        return ResponseEntity.ok(chatService.getChatRoomsByUserId(userId));
    }

    // 채팅방 정보 조회
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomDto> getChatRoom(@PathVariable String roomId) {
        return ResponseEntity.ok(chatService.getChatRoomByRoomId(roomId));
    }

    // 채팅 메시지 목록 조회
    @GetMapping("/messages/{roomId}")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(@PathVariable String roomId) {
        return ResponseEntity.ok(chatService.getChatMessages(roomId));
    }

    // REST API로 메시지 전송 (WebSocket 사용 불가능한 경우 대비)
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @Valid @RequestBody ChatMessageRequestDto messageDto) {
        return ResponseEntity.ok(chatService.sendMessage(
                ChatMessageDto.builder()
                        .roomId(messageDto.getRoomId())
                        .senderId(messageDto.getSenderId())
                        .content(messageDto.getContent())
                        .type(messageDto.getType())
                        .build()));
    }

    // 이미지 메시지 전송
    @PostMapping("/messages/image")
    public ResponseEntity<ImageUploadResponseDto> sendImageMessage(
            @RequestParam("roomId") String roomId,
            @RequestParam("senderId") Long senderId,
            @RequestParam("image") MultipartFile image) {
        
        try {
            String imageUrl = s3Service.uploadImage(image);
            
            // 이미지 메시지 저장 및 전송
            ChatMessageDto messageDto = ChatMessageDto.builder()
                    .roomId(roomId)
                    .senderId(senderId)
                    .content("[이미지]")
                    .imageUrl(imageUrl)
                    .type(MessageType.IMAGE)
                    .build();
            
            chatService.sendMessage(messageDto);
            
            return ResponseEntity.ok(ImageUploadResponseDto.builder()
                    .imageUrl(imageUrl)
                    .message("이미지가 성공적으로 업로드되었습니다.")
                    .build());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ImageUploadResponseDto.builder()
                            .message("이미지 업로드에 실패했습니다: " + e.getMessage())
                            .build());
        }
    }

    // WebSocket 메시지 핸들링 (STOMP)
    @MessageMapping("/chat.sendMessage")
    public void handleChatMessage(@Payload ChatMessageDto messageDto) {
        ChatMessageDto savedMessage = chatService.sendMessage(messageDto);
        messagingTemplate.convertAndSend("/topic/chat/" + messageDto.getRoomId(), savedMessage);
    }

    @MessageMapping("/chat.join")
    public void handleJoin(@Payload ChatMessageDto messageDto) {
        messageDto.setType(MessageType.JOIN);
        messageDto.setContent(messageDto.getSenderName() + "님이 입장했습니다.");
        ChatMessageDto savedMessage = chatService.sendMessage(messageDto);
        messagingTemplate.convertAndSend("/topic/chat/" + messageDto.getRoomId(), savedMessage);
    }

    @MessageMapping("/chat.leave")
    public void handleLeave(@Payload ChatMessageDto messageDto) {
        messageDto.setType(MessageType.LEAVE);
        messageDto.setContent(messageDto.getSenderName() + "님이 퇴장했습니다.");
        ChatMessageDto savedMessage = chatService.sendMessage(messageDto);
        messagingTemplate.convertAndSend("/topic/chat/" + messageDto.getRoomId(), savedMessage);
    }
}
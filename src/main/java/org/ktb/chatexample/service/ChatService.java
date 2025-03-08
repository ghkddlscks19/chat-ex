package org.ktb.chatexample.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.ktb.chatexample.dto.ChatMessageDto;
import org.ktb.chatexample.dto.ChatRoomCreateRequestDto;
import org.ktb.chatexample.dto.ChatRoomDto;
import org.ktb.chatexample.dto.MessageType;
import org.ktb.chatexample.entity.ChatMessage;
import org.ktb.chatexample.entity.ChatRoom;
import org.ktb.chatexample.entity.Post;
import org.ktb.chatexample.entity.User;
import org.ktb.chatexample.repository.ChatMessageRepository;
import org.ktb.chatexample.repository.ChatRoomRepository;
import org.ktb.chatexample.repository.PostRepository;
import org.ktb.chatexample.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final SimpMessagingTemplate messagingTemplate;
    
    // 채팅방 생성
    public ChatRoomDto createChatRoom(ChatRoomCreateRequestDto requestDto) {
        Post post = postRepository.findById(requestDto.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. ID: " + requestDto.getPostId()));
        
        User postAuthor = userRepository.findByUsername(post.getAuthor())
                .orElseThrow(() -> new EntityNotFoundException("게시글 작성자를 찾을 수 없습니다: " + post.getAuthor()));
        
        User requestUser = userRepository.findById(requestDto.getRequestUserId())
                .orElseThrow(() -> new EntityNotFoundException("요청자를 찾을 수 없습니다. ID: " + requestDto.getRequestUserId()));
        
        // 동일한 게시글, 사용자 간의 채팅방이 이미 존재하는지 확인
        ChatRoom existingRoom = chatRoomRepository.findByPostAndUser1AndUser2(post, postAuthor, requestUser)
                .orElse(null);
        
        if (existingRoom != null) {
            return convertToChatRoomDto(existingRoom);
        }
        
        // 새 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .post(post)
                .user1(postAuthor)
                .user2(requestUser)
                .roomId(UUID.randomUUID().toString())
                .build();
        
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        
        // JOIN 메시지 전송
        sendJoinMessage(savedChatRoom, requestUser);
        
        return convertToChatRoomDto(savedChatRoom);
    }
    
    // 사용자 ID로 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<ChatRoomDto> getChatRoomsByUserId(Long userId) {
        return chatRoomRepository.findChatRoomsByUserId(userId).stream()
                .map(this::convertToChatRoomDto)
                .collect(Collectors.toList());
    }
    
    // 채팅방 ID로 채팅방 조회
    @Transactional(readOnly = true)
    public ChatRoomDto getChatRoomByRoomId(String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다. RoomID: " + roomId));
        
        return convertToChatRoomDto(chatRoom);
    }
    
    // 채팅 메시지 저장 및 전송
    public ChatMessageDto sendMessage(ChatMessageDto messageDto) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(messageDto.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다. RoomID: " + messageDto.getRoomId()));
        
        User sender = userRepository.findById(messageDto.getSenderId())
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + messageDto.getSenderId()));
        
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(messageDto.getContent())
                .imageUrl(messageDto.getImageUrl())
                .type(messageDto.getType())
                .build();
        
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        ChatMessageDto savedMessageDto = convertToChatMessageDto(savedMessage);
        
        // WebSocket을 통해 메시지 전송
        messagingTemplate.convertAndSend("/topic/chat/" + messageDto.getRoomId(), savedMessageDto);
        
        return savedMessageDto;
    }
    
    // 이미지 메시지 전송
    public ChatMessageDto sendImageMessage(String roomId, Long senderId, MultipartFile image) throws IOException {
        String imageUrl = s3Service.uploadImage(image);
        
        ChatMessageDto imageMessage = ChatMessageDto.builder()
                .roomId(roomId)
                .senderId(senderId)
                .content("이미지 메시지")
                .imageUrl(imageUrl)
                .type(MessageType.IMAGE)
                .build();
        
        return sendMessage(imageMessage);
    }
    
    // 채팅방의 메시지 목록 조회
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatMessages(String roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다. RoomID: " + roomId));
        
        return chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom).stream()
                .map(this::convertToChatMessageDto)
                .collect(Collectors.toList());
    }
    
    // JOIN 메시지 전송 (내부 메서드)
    private void sendJoinMessage(ChatRoom chatRoom, User user) {
        ChatMessageDto joinMessage = ChatMessageDto.builder()
                .roomId(chatRoom.getRoomId())
                .senderId(user.getId())
                .senderName(user.getUsername())
                .content(user.getUsername() + "님이 채팅방에 입장했습니다.")
                .type(MessageType.JOIN)
                .createdAt(LocalDateTime.now())
                .build();
        
        // JOIN 메시지 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(user)
                .content(joinMessage.getContent())
                .type(MessageType.JOIN)
                .build();
        
        chatMessageRepository.save(chatMessage);
        
        // WebSocket을 통해 메시지 전송
        messagingTemplate.convertAndSend("/topic/chat/" + chatRoom.getRoomId(), joinMessage);
    }
    
    // ChatRoom 엔티티를 ChatRoomDto로 변환
    private ChatRoomDto convertToChatRoomDto(ChatRoom chatRoom) {
        // 마지막 메시지 정보 가져오기
        List<ChatMessage> lastMessages = chatMessageRepository.findLatestMessageByChatRoom(
                chatRoom, PageRequest.of(0, 1));
        
        String lastMessage = null;
        LocalDateTime lastMessageTime = null;
        
        if (!lastMessages.isEmpty()) {
            lastMessage = lastMessages.get(0).getContent();
            lastMessageTime = lastMessages.get(0).getCreatedAt();
        }
        
        return ChatRoomDto.builder()
                .id(chatRoom.getId())
                .roomId(chatRoom.getRoomId())
                .postId(chatRoom.getPost().getId())
                .postTitle(chatRoom.getPost().getTitle())
                .user1Id(chatRoom.getUser1().getId())
                .user1Name(chatRoom.getUser1().getUsername())
                .user2Id(chatRoom.getUser2().getId())
                .user2Name(chatRoom.getUser2().getUsername())
                .createdAt(chatRoom.getCreatedAt())
                .lastMessage(lastMessage)
                .lastMessageTime(lastMessageTime)
                .build();
    }
    
    // ChatMessage 엔티티를 ChatMessageDto로 변환
    private ChatMessageDto convertToChatMessageDto(ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .id(chatMessage.getId())
                .roomId(chatMessage.getChatRoom().getRoomId())
                .senderId(chatMessage.getSender().getId())
                .senderName(chatMessage.getSender().getUsername())
                .content(chatMessage.getContent())
                .imageUrl(chatMessage.getImageUrl())
                .type(chatMessage.getType())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}
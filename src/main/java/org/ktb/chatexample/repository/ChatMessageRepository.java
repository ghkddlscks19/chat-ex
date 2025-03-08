package org.ktb.chatexample.repository;

import org.ktb.chatexample.entity.ChatMessage;
import org.ktb.chatexample.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // 특정 채팅방의 모든 메시지 조회 (생성 시간 오름차순)
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom);
    
    // 특정 채팅방의 모든 메시지 조회 (페이징 처리)
    Page<ChatMessage> findByChatRoom(ChatRoom chatRoom, Pageable pageable);
    
    // 채팅방의 마지막 메시지 조회
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom = ?1 ORDER BY m.createdAt DESC")
    List<ChatMessage> findLatestMessageByChatRoom(ChatRoom chatRoom, Pageable pageable);
    
    // roomId로 마지막 메시지 조회
    @Query("SELECT m FROM ChatMessage m WHERE m.chatRoom.roomId = ?1 ORDER BY m.createdAt DESC")
    List<ChatMessage> findLatestMessageByRoomId(String roomId, Pageable pageable);
    
    // 메시지 수 카운트
    long countByChatRoom(ChatRoom chatRoom);
    
    // 특정 사용자가 전송한 메시지 조회
    List<ChatMessage> findBySender_Id(Long senderId);
}
package org.ktb.chatexample.repository;

import org.ktb.chatexample.entity.ChatRoom;
import org.ktb.chatexample.entity.Post;
import org.ktb.chatexample.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    // roomId로 채팅방 조회
    Optional<ChatRoom> findByRoomId(String roomId);
    
    // 특정 사용자가 참여한 모든 채팅방 조회
    List<ChatRoom> findByUser1OrUser2(User user1, User user2);
    
    // 특정 게시글에 대한 채팅방 조회
    List<ChatRoom> findByPost(Post post);
    
    // 특정 게시글과 두 사용자 간의 채팅방 조회
    Optional<ChatRoom> findByPostAndUser1AndUser2(Post post, User user1, User user2);
    
    // 사용자 ID로 참여중인 모든 채팅방 조회
    @Query("SELECT c FROM ChatRoom c WHERE c.user1.id = ?1 OR c.user2.id = ?1")
    List<ChatRoom> findChatRoomsByUserId(Long userId);
    
    // 특정 게시글과 관련된 채팅방이 존재하는지 확인
    boolean existsByPost(Post post);
}
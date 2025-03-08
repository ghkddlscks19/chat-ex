package org.ktb.chatexample.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @OneToMany(mappedBy = "sender")
    private List<ChatMessage> sentMessages = new ArrayList<>();
    
    @OneToMany(mappedBy = "user1")
    private List<ChatRoom> chatRooms1 = new ArrayList<>();
    
    @OneToMany(mappedBy = "user2")
    private List<ChatRoom> chatRooms2 = new ArrayList<>();
}
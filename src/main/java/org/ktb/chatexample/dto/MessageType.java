package org.ktb.chatexample.dto;

/**
 * 채팅 메시지 타입을 정의하는 enum
 * DTO와 Entity에서 공통으로 사용
 */
public enum MessageType {
    CHAT,   // 일반 채팅 메시지
    JOIN,   // 입장 메시지
    LEAVE,  // 퇴장 메시지
    IMAGE   // 이미지 메시지
}
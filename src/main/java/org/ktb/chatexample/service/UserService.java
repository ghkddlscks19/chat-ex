package org.ktb.chatexample.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.ktb.chatexample.dto.UserDto;
import org.ktb.chatexample.dto.UserResponseDto;
import org.ktb.chatexample.entity.User;
import org.ktb.chatexample.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    // 모든 사용자 조회
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    // 사용자 ID로 조회
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + id));
        return convertToResponseDto(user);
    }
    
    // 사용자명으로 조회
    @Transactional(readOnly = true)
    public UserResponseDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. 사용자명: " + username));
        return convertToResponseDto(user);
    }
    
    // 사용자 등록
    public UserResponseDto registerUser(UserDto userDto) {
        // 사용자명 중복 체크
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new EntityExistsException("이미 사용 중인 사용자명입니다: " + userDto.getUsername());
        }
        
        // 이메일 중복 체크
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new EntityExistsException("이미 사용 중인 이메일입니다: " + userDto.getEmail());
        }
        
        // 비밀번호 암호화는 실제 구현 시 추가 필요
        User user = User.builder()
                .username(userDto.getUsername())
                .password(userDto.getPassword()) // 실제로는 암호화 필요
                .email(userDto.getEmail())
                .build();
        
        User savedUser = userRepository.save(user);
        return convertToResponseDto(savedUser);
    }
    
    // 사용자 정보 수정
    public UserResponseDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + id));
        
        // 이메일 변경 시 중복 체크
        if (!user.getEmail().equals(userDto.getEmail()) && userRepository.existsByEmail(userDto.getEmail())) {
            throw new EntityExistsException("이미 사용 중인 이메일입니다: " + userDto.getEmail());
        }
        
        // 사용자명 변경 시 중복 체크
        if (!user.getUsername().equals(userDto.getUsername()) && userRepository.existsByUsername(userDto.getUsername())) {
            throw new EntityExistsException("이미 사용 중인 사용자명입니다: " + userDto.getUsername());
        }
        
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            // 실제로는 암호화 필요
            user.setPassword(userDto.getPassword());
        }
        
        User updatedUser = userRepository.save(user);
        return convertToResponseDto(updatedUser);
    }
    
    // 사용자 삭제
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + id);
        }
        userRepository.deleteById(id);
    }
    
    // User 엔티티를 UserResponseDto로 변환
    private UserResponseDto convertToResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
package org.ktb.chatexample.controller;

import lombok.RequiredArgsConstructor;
import org.ktb.chatexample.dto.ImageUploadResponseDto;
import org.ktb.chatexample.service.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FileController {

    private final S3Service s3Service;

    // 단일 파일 업로드
    @PostMapping("/upload")
    public ResponseEntity<ImageUploadResponseDto> uploadFile(
            @RequestParam("file") MultipartFile file) {

        try {
            String fileUrl = s3Service.uploadImage(file);

            return ResponseEntity.ok(ImageUploadResponseDto.builder()
                    .imageUrl(fileUrl)
                    .message("파일이 성공적으로 업로드되었습니다.")
                    .build());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ImageUploadResponseDto.builder()
                            .message("파일 업로드에 실패했습니다: " + e.getMessage())
                            .build());
        }
    }

    // 파일 삭제
    @DeleteMapping
    public ResponseEntity<Void> deleteFile(@RequestParam String fileUrl) {
        s3Service.deleteFile(fileUrl);
        return ResponseEntity.noContent().build();
    }
}
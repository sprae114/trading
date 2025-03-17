package com.backend.common.service;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;


    // 파일 업로드
    public List<String> uploadFiles(MultipartFile[] files) throws IOException {

        return Stream.of(files)
                .filter(file -> file != null && !file.isEmpty()) // null이 아니고 비어있지 않은 파일만 필터링
                .map(file -> {
                    try {
                        return uploadImage(file);
                    } catch (IOException e) {
                        throw new CustomException(ErrorCode.AWS_S3_UPLOAD_FAIL, e.getMessage());
                    }
                })
                .collect(Collectors.toList());
    }

    // 파일 다운로드
    public List<byte[]> downloadFiles(List<String> fileNames) {
        return fileNames.stream()
                .filter(fileName -> fileName != null && !fileName.isEmpty())
                .map(this::downloadImage)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 첫번째 찾은 파일만 가져오기
    public byte[] downloadFirstFile(String fileName) {
        return downloadImage(fileName);
    }

    // 단일 파일 삭제
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (Exception e) {
            log.error("버킷 '{}'에서 파일 삭제 실패: {}", bucketName, fileName, e);
            throw new CustomException(ErrorCode.AWS_S3_DELETE_FAIL, e.getMessage());
        }
    }

    // 다중 파일 삭제
    public void deleteFiles(List<String> fileNames) {
        if (fileNames == null || fileNames.isEmpty()) {
            return;
        }

        List<ObjectIdentifier> keys = fileNames.stream()
                .filter(fileName -> fileName != null && !fileName.isEmpty())
                .map(fileName -> ObjectIdentifier.builder().key(fileName).build())
                .collect(Collectors.toList());

        if (keys.isEmpty()) {
            return;
        }

        try {
            DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(keys).build())
                    .build();

            DeleteObjectsResponse response = s3Client.deleteObjects(deleteObjectsRequest);
        } catch (Exception e) {
            log.error("버킷 '{}'에서 파일 삭제 실패", bucketName, e);
            throw new CustomException(ErrorCode.AWS_S3_DELETE_FAIL, e.getMessage());
        }
    }


    private String uploadImage(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileName)).toString();
    }

    private byte[] downloadImage(String fileName) throws NoSuchKeyException {
       try {
           GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                   .bucket(bucketName)
                   .key(fileName)
                   .build();

           return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
       }catch (Exception e) { // 파일이 존재하지 않으면 로그 남기고 null
           log.warn("File not found in bucket '{}': {}", bucketName, fileName);
           return null;
       }
    }
}
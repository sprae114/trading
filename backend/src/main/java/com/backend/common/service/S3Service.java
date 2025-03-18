package com.backend.common.service;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * aws s3의 CRUD 서비스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;


    /**
     * 파일들 업로드
     */
    public List<String> uploadFiles(MultipartFile[] files) throws IOException {

        if (files == null || files.length == 0) {
            log.debug("No files provided for upload to bucket: {}", bucketName);
            return List.of();
        }


        List<String> uploadedUrls = Stream.of(files)
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> {
                    try {
                        String url = uploadImage(file);
                        log.debug("Uploaded file '{}' to bucket '{}'", file.getOriginalFilename(), bucketName);
                        return url;
                    } catch (IOException e) {
                        log.warn("Failed to upload file '{}' to bucket '{}'", file.getOriginalFilename(), bucketName, e);
                        throw new CustomException(ErrorCode.AWS_S3_UPLOAD_FAIL, e.getMessage());
                    }
                })
                .collect(Collectors.toList());

        log.info("Successfully uploaded {} files to bucket '{}'", uploadedUrls.size(), bucketName);
        return uploadedUrls;
    }

    /**
     * 파일 다운로드
     */
    public List<byte[]> downloadFiles(List<String> fileNames) {

        if (fileNames == null || fileNames.isEmpty()) {
            log.debug("No file names provided for download from bucket '{}'", bucketName);
            return List.of();
        }

        List<byte[]> downloadedFiles = fileNames.stream()
                .filter(fileName -> fileName != null && !fileName.isEmpty())
                .map(this::downloadImage)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.info("Successfully downloaded {} files from bucket '{}'", downloadedFiles.size(), bucketName);
        return downloadedFiles;
    }

    /**
     * 첫번째 찾은 파일만 가져오기
     */
    public byte[] downloadFirstFile(String fileName) {
        byte[] fileData = downloadImage(fileName);

        if (fileData != null) {
            log.info("Downloading first file '{}' from bucket '{}'", fileName, bucketName);
        }

        return fileData;
    }

    /**
     * 단일 파일 삭제
     */
    public void deleteFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            log.debug("Attempted to delete null or empty file name from bucket '{}'", bucketName);
            return;
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted file '{}' from bucket '{}'", fileName, bucketName);

        } catch (Exception e) {
            log.error("Failed to delete file '{}' from bucket '{}'", fileName, bucketName, e);
            throw new CustomException(ErrorCode.AWS_S3_DELETE_FAIL, e.getMessage());
        }
    }

    /**
     * 다중 파일 삭제
     */
    public void deleteFiles(List<String> fileNames) {
        if (fileNames == null || fileNames.isEmpty()) {
            log.debug("No file names provided for deletion from bucket '{}'", bucketName);
            return;
        }

        List<ObjectIdentifier> keys = fileNames.stream()
                .filter(fileName -> fileName != null && !fileName.isEmpty())
                .map(fileName -> ObjectIdentifier.builder().key(fileName).build())
                .collect(Collectors.toList());

        if (keys.isEmpty()) {
            log.debug("No valid file names to delete from bucket '{}'", bucketName);
            return;
        }

        try {
            DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(keys).build())
                    .build();

            s3Client.deleteObjects(deleteObjectsRequest);
            log.info("Successfully deleted {} files from bucket '{}'", keys.size(), bucketName);
        } catch (Exception e) {

            log.error("Failed to delete files from bucket '{}': {}", bucketName, fileNames, e);
            throw new CustomException(ErrorCode.AWS_S3_DELETE_FAIL, e.getMessage());
        }
    }


    private String uploadImage(MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        log.debug("Uploading file '{}' to bucket '{}'", fileName, bucketName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
        String url = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileName)).toString();
        log.debug("Generated URL for file '{}': {}", fileName, url);
        return url;
    }

    private byte[] downloadImage(String fileName) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            byte[] fileData = s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
            log.debug("Downloaded file '{}' from bucket '{}', size: {} bytes", fileName, bucketName, fileData.length);
            return fileData;
        } catch (Exception e) {
            log.warn("File '{}' not found or error occurred in bucket '{}'", fileName, bucketName, e);
            return null;
        }
    }
}
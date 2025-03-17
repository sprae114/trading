package com.backend.common.service;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.core.ResponseBytes;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Utilities s3Utilities;

    @InjectMocks
    private S3Service s3Service;


    @Test
    @DisplayName("파일 업로드  : 성공")
    void uploadFiles_Success() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes());
        MultipartFile[] files = {file};

        // S3Utilities 모킹
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl((Consumer<GetUrlRequest.Builder>) any())).thenReturn(new URL("http://test-bucket.s3.amazonaws.com/test.txt"));

        // When
        List<String> result = s3Service.uploadFiles(files);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertTrue(result.get(0).contains("test.txt"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }


    @Test
    @DisplayName("파일 업로드 : 성공(빈 파일 목록 업로드)")
    void uploadFiles_EmptyFiles() throws IOException {
        // Given
        MultipartFile[] files = {};

        // When
        List<String> result = s3Service.uploadFiles(files);

        // Then
        assertTrue(result.isEmpty());
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }


    @Test
    @DisplayName("파일 업로드 : 실패(IOException 발생)")
    void uploadFiles_Failure_IOException() throws IOException {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        MultipartFile[] files = {file};

        when(file.getBytes()).thenThrow(new IOException("IO Error"));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                s3Service.uploadFiles(files)
        );

        assertEquals(ErrorCode.AWS_S3_UPLOAD_FAIL, exception.getErrorCode());
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }


    @Test
    @DisplayName("파일 다운로드 : 성공")
    void downloadFiles_Success() {
        // Given
        String fileName = "test.txt";
        byte[] expectedBytes = "test content".getBytes();

        ResponseBytes mockResponse = mock(ResponseBytes.class);
        when(mockResponse.asByteArray()).thenReturn(expectedBytes);
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(mockResponse);

        // When
        List<byte[]> result = s3Service.downloadFiles(Collections.singletonList(fileName));

        // Then
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertArrayEquals(expectedBytes, result.get(0));
        verify(s3Client, times(1)).getObjectAsBytes(any(GetObjectRequest.class));
    }


    @Test
    @DisplayName("파일 다운로드 : 실패(null)")
    void downloadFiles_Failure_null() {
        // Given
        String fileName = null;

        List<byte[]> result = s3Service.downloadFiles(Collections.singletonList(fileName));

        assertTrue(result.isEmpty());
        verify(s3Client, never()).getObjectAsBytes(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("파일 다운로드 : 실패(존재하지 않는 키)")
    void downloadFiles_Failure_Exception() {
        // given
        String fileName = "noKey.txt";

        // S3Exception 모킹 (파일이 존재하지 않는 상황)
        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder()
                        .message("The specified key does not exist")
                        .statusCode(404)
                        .build());

        // when
        List<byte[]> result = s3Service.downloadFiles(Collections.singletonList(fileName));

        // then
        assertTrue(result.isEmpty());
        verify(s3Client, times(1)).getObjectAsBytes(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("단일 파일 삭제: 성공")
    void deleteFile_Success() {
        // Given
        String fileName = "test.txt";

        // When
        s3Service.deleteFile(fileName);

        // Then
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("단일 파일 삭제: 실패(유효하지 않은 파일 이름)")
    void deleteFile_Failure_InvalidName() {
        // Given
        String fileName = null;

        // When
        s3Service.deleteFile(fileName);

        // Then
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("단일 파일 삭제: 실패(S3 예외)")
    void deleteFile_Failure_Exception() {
        // Given
        String fileName = "test.txt";
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("Access Denied").statusCode(403).build());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                s3Service.deleteFile(fileName)
        );

        assertEquals(ErrorCode.AWS_S3_DELETE_FAIL, exception.getErrorCode());
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("다중 파일 삭제: 성공")
    void deleteFiles_Success() {
        // Given
        List<String> fileNames = List.of("test1.txt", "test2.txt");

        when(s3Client.deleteObjects(any(DeleteObjectsRequest.class))).thenReturn(mock(DeleteObjectsResponse.class));

        // When
        s3Service.deleteFiles(fileNames);

        // Then
        verify(s3Client, times(1)).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    @DisplayName("다중 파일 삭제: 성공(빈 파일 목록)")
    void deleteFiles_EmptyFiles() {
        // Given
        List<String> fileNames = Collections.emptyList();

        // When
        s3Service.deleteFiles(fileNames);

        // Then
        verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    @DisplayName("다중 파일 삭제: 실패(S3 예외)")
    void deleteFiles_Failure_Exception() {
        // Given
        List<String> fileNames = List.of("test1.txt", "test2.txt");
        when(s3Client.deleteObjects(any(DeleteObjectsRequest.class)))
                .thenThrow(S3Exception.builder().message("Access Denied").statusCode(403).build());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                s3Service.deleteFiles(fileNames)
        );

        assertEquals(ErrorCode.AWS_S3_DELETE_FAIL, exception.getErrorCode());
        verify(s3Client, times(1)).deleteObjects(any(DeleteObjectsRequest.class));
    }
}
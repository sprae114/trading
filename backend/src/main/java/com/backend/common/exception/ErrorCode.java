package com.backend.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "적절하지 않은 요청 값입니다"),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "인증이 유효하지 않습니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다"),

    ENVIRONMENT_VARIABLE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "환경 변수를 찾을 수 없습니다"),
    INVALID_OTP(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다"),

    DUPLICATED_USER_NAME(HttpStatus.BAD_REQUEST, "중복된 사용자 이름입니다"),
    DUPLICATED_EMAIL(HttpStatus.BAD_REQUEST, "중복된 이메일입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    USER_NOT_AUTHORIZED(HttpStatus.UNAUTHORIZED, "사용자 권한이 없습니다"),
    USER_NOT_MATCH(HttpStatus.UNAUTHORIZED, "사용자 정보가 일치하지 않습니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다"),
    AUTHORITY_NOT_FOUND(HttpStatus.NOT_FOUND, "권한을 찾을 수 없습니다"),
    DUPLICATED_AUTHORITY(HttpStatus.BAD_REQUEST, "중복된 권한입니다"),

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다"),

    ALREADY_LIKED_POST(HttpStatus.CONFLICT, "이미 좋아요를 했습니다"),
    LIKES_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 좋아요를 찾을 수 없습니다"),

    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 채팅방을 찾을 수 없습니다"),
    ALREADY_CHATROOM(HttpStatus.CONFLICT, "이미 채팅방이 존재합니다"),

    NOTIFICATION_CONNECT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SSE 연결을 실패했습니다"),
    SSE_REQUEST_GET_ERROR(HttpStatus.BAD_REQUEST, "SSE의 잘못된 요청입니다"),
    SSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SSE 에러가 발생했습니다"),

    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 에러가 발생했습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러가 발생했습니다"),

    AWS_S3_UPLOAD_FAIL(HttpStatus.BAD_REQUEST , "파일 업로드 중 오류 발생했습니다"),
    AWS_S3_DOWNLOAD_FAIL(HttpStatus.BAD_REQUEST, "파일 다운로드 중 오류 발생했습니다"),
    AWS_S3_NOT_FOUND_KEY(HttpStatus.NOT_FOUND, "해당 파일이 존재하지 않습니다"),
    AWS_S3_DELETE_FAIL(HttpStatus.NOT_FOUND, "해당 파일 삭제에서 오류가 발생했습니다"),

    SERIALIZER_ERROR(HttpStatus.BAD_REQUEST, "직렬화에서 에러가 발생했습니다"),
    DESERIALIZER_ERROR(HttpStatus.BAD_REQUEST, "역직렬화에서 에러가 발생했습니다"),

    JSON_PASSING_ERROR(HttpStatus.BAD_REQUEST, "json 파싱하는 중에 에러가 발생했습니다"),
    ;

    private final HttpStatus status;
    private final String message;
}

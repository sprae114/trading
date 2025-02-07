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

    DUPLICATED_USER_NAME(HttpStatus.BAD_REQUEST, "중복된 사용자 이름입니다"),
    DUPLICATED_EMAIL(HttpStatus.BAD_REQUEST, "중복된 이메일입니다"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
    USER_NOT_AUTHORIZED(HttpStatus.UNAUTHORIZED, "사용자 권한이 없습니다"),
    USER_NOT_MATCH(HttpStatus.UNAUTHORIZED, "사용자 정보가 일치하지 않습니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다"),
    AUTHORITY_NOT_FOUND(HttpStatus.NOT_FOUND, "권한을 찾을 수 없습니다"),
    DUPLICATED_AUTHORITY(HttpStatus.BAD_REQUEST, "중복된 권한입니다"),

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다"),

    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 에러가 발생했습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러가 발생했습니다"),
    ;

    private final HttpStatus status;
    private final String message;
}

package com.backend.common.controller;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.common.util.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 기본 SSE 테스트용도
 */
@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SSEController {

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 60분

    private final SseEmitterRegistry emitters;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitters.add(emitter);

        // 연결 종료, 타임아웃, 에러 발생 시 리스트에서 제거
        emitter.onCompletion(() ->
                log.info("SSE Connected close"));

        emitter.onTimeout(() -> {
            log.info("SSE Connected Timeout");
            emitter.complete();
        });

        emitter.onError((e) -> {
            emitter.completeWithError(e);
            throw new CustomException(ErrorCode.SSE_ERROR, e.getMessage());
        });

        try {
            log.info("SSE Subscribed");
            emitter.send(SseEmitter.event().name("connection").data("연결 완료"));
        } catch (IOException e) {
            emitter.completeWithError(e);
            throw new CustomException(ErrorCode.SSE_ERROR, e.getMessage());
        }

        return emitter;
    }

    @PostMapping("/alarm")
    public void triggerAlarm(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");

        if (message == null) {
            throw new CustomException(ErrorCode.SSE_REQUEST_GET_ERROR, message);
        }

        kafkaTemplate.send("alarmTopic", message);
    }
}

package com.backend.common.util;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SseEmitterRegistry {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void add(SseEmitter emitter) {
        emitters.add(emitter);
    }

    public void remove(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    public List<SseEmitter> getEmitters() {
        return emitters;
    }
}


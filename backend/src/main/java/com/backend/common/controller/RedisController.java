package com.backend.common.controller;

import com.backend.common.model.RedisRequest;
import com.backend.common.service.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * postman으로 redis 추가 및 삭제 위한 컨트롤러
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/redis")
public class RedisController {

    private final RedisService redisService;
    private final ObjectMapper objectMapper;


    @GetMapping
    public String get(@RequestParam String key) {
        return redisService.get(key).toString();
    }

    @PostMapping
    public String post(@RequestBody RedisRequest redisRequest) throws JsonProcessingException {
        String key = "post:" + redisRequest.id();
        redisService.save(key, objectMapper.writeValueAsString(redisRequest));
        return "저장완료";
    }

    @DeleteMapping("/delete")
    public String deleteData(@RequestParam String key) {
        redisService.delete(key);

        return "삭제완료 : " + key;
    }
}

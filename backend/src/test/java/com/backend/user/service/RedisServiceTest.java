package com.backend.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RedisServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisService redisService;

    @BeforeEach
    public void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations); // opsForValue()가 ValueOperations를 반환하도록 설정
    }


    @DisplayName("Redis에 데이터 저장 성공")
    @Test
    public void testSave() {
        // given
        String key = "testKey";
        String value = "testValue";

        // when
        redisService.save(key, value);

        // then
        verify(valueOperations).set(key, value); // valueOperations의 set 메서드를 검증
    }

    @DisplayName("Redis에서 데이터 조회 성공")
    @Test
    public void testGet() {
        // given
        String key = "testKey";
        String expectedValue = "testValue";
        when(valueOperations.get(key)).thenReturn(expectedValue); // valueOperations의 get 메서드 모의

        // when
        String actualValue = redisService.get(key);

        // then
        verify(valueOperations).get(key); // get 메서드 호출 여부 검증
        assertEquals(expectedValue, actualValue); // 반환값 검증
    }

    @DisplayName("Redis에 데이터 저장 및 만료 시간 설정 성공")
    @Test
    public void testSetKeyWithExpiration() {
        // given
        String key = "testKey";
        String value = "testValue";
        long timeout = 300L;

        // when
        redisService.setKeyWithExpiration(key, value);

        // then
        verify(valueOperations).set(key, value, timeout, TimeUnit.SECONDS); // set 메서드 호출 여부 검증
    }
}

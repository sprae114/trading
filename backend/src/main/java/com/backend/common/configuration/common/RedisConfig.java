package com.backend.common.configuration.common;

import com.backend.common.service.RedisSubscriber;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static com.fasterxml.jackson.databind.SerializationFeature.*;

/**
 * 레디스 설정 파일
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisConnectionFactory connectionFactory;
    private final RedisSubscriber redisSubscriber;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("Initializing redisTemplate");
        // ObjectMapper 설정
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Java 8 날짜/시간 모듈 추가
        objectMapper.disable(WRITE_DATES_AS_TIMESTAMPS); // 타임스탬프 대신 문자열 사용

        // RedisTemplate 설정
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer()); // 키는 문자열
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)); // 커스터마이징된 ObjectMapper 사용
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.afterPropertiesSet();

        return template;
    }

    // 추가: StringRedisTemplate 등록 (문자열 메시지 전송용)
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("Initializing StringRedisTemplate");
        return new StringRedisTemplate(redisConnectionFactory);
    }

    // 추가: Redis 채널 주제 설정
    @Bean
    public ChannelTopic chatChannelTopic() {
        log.info("Creating ChannelTopic: chat");
        return new ChannelTopic("chat");
    }

    // 추가: Redis pub/sub 리스너 등록
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListener redisSubscriber,
            ChannelTopic chatChannelTopic
    ) {
        log.info("Initializing RedisMessageListenerContainer for channel: {}", chatChannelTopic.getTopic());
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(redisSubscriber, chatChannelTopic);
        return container;
    }
}
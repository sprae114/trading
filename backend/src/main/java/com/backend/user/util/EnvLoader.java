package com.backend.user.util;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;


@Component
public class EnvLoader {

    private Dotenv dotenv;

    @PostConstruct
    public void init() {
        dotenv = Dotenv.load();
        System.setProperty("DB_USERNAME", dotenv.get("NAVER_MAIL_ID"));
        System.setProperty("DB_PASSWORD", dotenv.get("NAVER_MAIL_PASSWORD"));
    }

    public String get(String key) {
        return dotenv.get(key);
    }
}

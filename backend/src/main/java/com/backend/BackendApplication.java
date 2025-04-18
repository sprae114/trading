package com.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@SpringBootApplication
@EntityScan(basePackages =
		{
				"com.backend.user.model.entity" ,
				"com.backend.post.model.entity"
		}
)
@EnableJpaRepositories(basePackages = {
		"com.backend.user.repository" ,
		"com.backend.post.repository"
})


@EnableMongoRepositories(basePackages = {
		"com.backend.chat.repository"
})

@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}

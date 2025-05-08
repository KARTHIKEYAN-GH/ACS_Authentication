package com.acs.authentication.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.acs.web.dto.SessionDetails;

@Configuration
public class RedisConfig {

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		Jackson2JsonRedisSerializer<SessionDetails> sessionDetailsSerializer = new Jackson2JsonRedisSerializer<>(
				SessionDetails.class);
		template.setValueSerializer(sessionDetailsSerializer);
		template.setKeySerializer(RedisSerializer.string());

		return template;
	}
}

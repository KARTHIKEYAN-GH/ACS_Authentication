package com.acs.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.acs.web.dto.SessionDetails;

@Configuration
public class RedisConfig {

	    @Bean
	    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
	        RedisTemplate<String, Object> template = new RedisTemplate<>();
	        template.setConnectionFactory(redisConnectionFactory);

	        // Create a Jackson2JsonRedisSerializer specifically for SessionDetails
	        Jackson2JsonRedisSerializer<SessionDetails> sessionDetailsSerializer = new Jackson2JsonRedisSerializer<>(SessionDetails.class);

	        // Set the default serializer to Jackson2JsonRedisSerializer for values
	        template.setValueSerializer(sessionDetailsSerializer);

	        // Set the default serializer for keys (using String RedisSerializer for keys)
	        template.setKeySerializer(RedisSerializer.string());

	        return template;
	    }
	}



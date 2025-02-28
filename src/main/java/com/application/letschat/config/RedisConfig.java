package com.application.letschat.config;

import com.application.letschat.dto.chatRoomUser.ChatRoomUserDTO;
import com.application.letschat.dto.message.MessageDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, MessageDTO> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, MessageDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(MessageDTO.class));
        return template;
    }

    @Bean
    public RedisTemplate<String, ChatRoomUserDTO> chatRoomUserRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, ChatRoomUserDTO> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(ChatRoomUserDTO.class));
        return template;

    }
}

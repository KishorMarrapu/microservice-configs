package com.OTRAS.DemoProject.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ActiveJwtService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ACTIVE_JWT_KEY_PREFIX = "active-jwt:user:";

    public void storeActiveJwt(Long userId, String jti, long expirationMs) {
        String key = ACTIVE_JWT_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, jti, expirationMs, TimeUnit.MILLISECONDS);
    }

    public String getActiveJwt(Long userId) {
        String key = ACTIVE_JWT_KEY_PREFIX + userId;
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void revokeActiveJwt(Long userId) {
        String key = ACTIVE_JWT_KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }
}
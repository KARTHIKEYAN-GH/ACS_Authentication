package com.acs.authentication.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    @Cacheable(value = "sessionCache", key = "#userId")
    public String getSessionKey(String userId) {
        // Logic to fetch sessionKey from Redis or any other data source
        return "someSessionKey";
    }

    @CachePut(value = "sessionCache", key = "#userId")
    public String updateSessionKey(String userId, String newSessionKey) {
        // Logic to update sessionKey in Redis
        return newSessionKey;
    }

    @CacheEvict(value = "sessionCache", key = "#userId")
    public void removeSessionKey(String userId) {
        // Logic to remove sessionKey from Redis
    }
}

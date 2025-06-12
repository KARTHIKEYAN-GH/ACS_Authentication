package com.acs.authentication.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtSessionFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();

		// if (path.startsWith("/api/acs/")) {
		// return true;
		// }

		if (path.equals("/api/cloudstack/login") || path.equals("/api/cloudstack/refresh")) {
			return true;
		}
		return false;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authHeader = request.getHeader("Authorization");
		
		 // CORS headers manually added
	    response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
	    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
	    response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
	    response.setHeader("Access-Control-Allow-Credentials", "true");

	    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
	        response.setStatus(HttpServletResponse.SC_OK);
	        return;
	    }

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Missing token");
			return;
		};

		try {
		    SessionInfo sessioninfo = jwtUtil.extractSessionInfo(authHeader);
		    String sessionKey = sessioninfo.getSessionKey();  // This is your actual Redis key
		    String redisKey = "session:" + sessionKey;
		    Object sessionData = redisTemplate.opsForValue().get(redisKey);

		    Long oldTtl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
	        System.out.println("Old TTL: " + oldTtl);
		    // Check if session exists in Redis
		    if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
		        redisTemplate.expire(redisKey, 15, TimeUnit.MINUTES);
		        System.out.println("Session TTL renewed to 15 minutes for: " + sessionKey);
		        System.out.println("=====================================================");	      
		        Long newTtl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
		        System.out.println("New TTL: " + newTtl);      
		    } else {
		        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		        response.getWriter().write("Session expired or invalid.");
		        return;
		    }		    
		    if (sessionData != null) {
		        redisTemplate.expire(redisKey, 15, TimeUnit.MINUTES);
		        System.out.println("Session TTL renewed for 15 minutes for sessionKey: " + sessionKey);
		    } else {
		        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		        response.getWriter().write("Session expired or invalid.");
		        return;
		    }

		    request.setAttribute("session", sessioninfo); // Optional: pass to controller
		} catch (Exception e) {
		    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		    response.getWriter().write("Invalid or expired token.");
		    return;
		}

		filterChain.doFilter(request, response);
	}
}

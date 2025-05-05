package com.acs.authentication.util;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

	@Value("${security.jwt.secret-key}")
	private String secretKeyString;

	private SecretKey secretKey;

	private static final long EXPIRATION_TIME = 3600 * 1000; // 1 hour

	@PostConstruct
	public void init() {
		this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
	}

	public String generateToken(String username, String sessionKey) {
		return Jwts.builder()
				.setSubject(username)
				.claim("sessionKey", sessionKey)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(secretKey, SignatureAlgorithm.HS256).compact();
	}

	public Claims parseToken(String token) {
		return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
	}

	public SessionInfo extractSessionInfo(String authHeader) {
		String token = authHeader.replace("Bearer ", "");

		Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();

		String sessionKey = claims.get("sessionKey", String.class);
		String username = claims.getSubject();

		return new SessionInfo(username, sessionKey);
	}
}

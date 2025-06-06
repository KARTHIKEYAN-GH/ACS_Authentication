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

	private static final long ACCESSTOKEN_EXPIRATION_TIME = 10 * 1000; // 10 mins

	private static final long REFRESHTOKEN_EXPIRATION_TIME = 20 * 1000; // 20 mins

	@PostConstruct
	public void init() {
		this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
	}

	public String generateToken(String username, String sessionKey) {
		return Jwts.builder().setSubject(username).claim("sessionKey", sessionKey).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + ACCESSTOKEN_EXPIRATION_TIME))
				.signWith(secretKey, SignatureAlgorithm.HS256).compact();
	}

	public String generateRefreshToken(String username) {
		return Jwts.builder().setSubject(username).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + REFRESHTOKEN_EXPIRATION_TIME))
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

	public boolean isTokenExpired(String token) {
		try {
			Claims claims = parseToken(token);
			return claims.getExpiration().before(new Date());
		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			return true;
		} catch (Exception e) {
			return true; // Treat other parsing errors as expired or invalid
		}
	}

	public String getSubject(String token) {
		try {
			return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			return e.getClaims().getSubject(); // Extract even if expired
		} catch (Exception e) {
			return null;  // Invalid or malformed token
		}
	}

}

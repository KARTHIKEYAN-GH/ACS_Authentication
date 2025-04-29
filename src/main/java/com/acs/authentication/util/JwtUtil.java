package com.acs.authentication.util;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

	//@Value("${security.jwt.secret-key}")
	//private static SecretKey secretKey;

	//@Value("${security.jwt.expiration-time}")
	//private static long jwtExpiration;

	 private static final long EXPIRATION_TIME = 3600; // 1 hour 
	// milliseconds
	 private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

	public static String generateToken(String username, String userId) {
		return Jwts.builder()
				.setSubject(username)
				.claim("userid", userId)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(SECRET_KEY)
				.compact();
	}
	public static Claims parseToken(String token) {
	    return Jwts.parserBuilder()
	            .setSigningKey(SECRET_KEY)
	            .build()
	            .parseClaimsJws(token)
	            .getBody();
	}

}

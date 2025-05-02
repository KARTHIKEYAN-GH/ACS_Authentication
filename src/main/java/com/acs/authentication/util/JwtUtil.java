package com.acs.authentication.util;

import java.util.Date;


import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
@Component
public class JwtUtil {

	//@Value("${security.jwt.secret-key}")
	//private static SecretKey secretKey;

	//@Value("${security.jwt.expiration-time}")
	//private static long jwtExpiration;

	 private static final long EXPIRATION_TIME = 3600 * 1000; // 1 hour 
	// milliseconds
	 private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

	public static String generateToken(String username, String userId,String sessionKey) {
		return Jwts.builder()
				.setSubject(username)
				.claim("userId", userId)
	            .claim("sessionKey", sessionKey)
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
	

//    public Claims extractClaims(String token) {
//        return Jwts.parser()
//            .setSigningKey(SECRET_KEY)
//            .parseClaimsJws(token)
//            .getBody();
//    }

    public String extractUserId(String token) {
        return parseToken(token).get("userId", String.class);
    }

    public String extractSessionKey(String token) {
        return parseToken(token).get("sessionKey", String.class);
    }

}

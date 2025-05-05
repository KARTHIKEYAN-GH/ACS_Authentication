package com.acs.authentication.util;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
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

//	@Override
//	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
//		String path = request.getRequestURI();
//		    // ✅ Skip only this exact login path
//		    return path != null && path.equals("/api/cloudstack/login");
//	}
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		 String path = request.getRequestURI();

		    if (path.startsWith("/api/acs/")) {
		        return true;
		    }

		    if (path.equals("/api/cloudstack/login")) {
		        return true;
		    }

		    return false;
		}


	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Missing token");
			return;
		}

		try {
			// SessionInfo session = jwtUtil.extractSessionInfo(authHeader);
			SessionInfo session = jwtUtil.extractSessionInfo(authHeader);
			request.setAttribute("session", session); // Set session info to request
		} catch (Exception e) {
			e.printStackTrace(); // Print full stacktrace for debugging
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Invalid token: " + e.getMessage()); // show exact cause
			return;
		}

		filterChain.doFilter(request, response);
	}
}

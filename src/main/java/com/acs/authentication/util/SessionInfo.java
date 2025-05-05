package com.acs.authentication.util;

public class SessionInfo {
	private String username;
	private String sessionKey;

	public SessionInfo(String username, String sessionKey) {
		this.username = username;
		this.sessionKey = sessionKey;
	}

	public String getUsername() {
		return username;
	}

	public String getSessionKey() {
		return sessionKey;
	}
}

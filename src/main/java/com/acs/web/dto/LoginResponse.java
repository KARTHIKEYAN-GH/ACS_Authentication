package com.acs.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
	private String username;
    private String userid;
    private String domainid;
    private int timeout;
    private String account;
    private String firstname;
    private String lastname;
    private String type;
    private String timezone;
    private String timezoneoffset;
    private String registered;
    private String sessionkey;
    private String is2faenabled;
    private String is2faverified;
    private String issuerfor2fa;
    private String errorMessage;
	private String jsessionid;
}

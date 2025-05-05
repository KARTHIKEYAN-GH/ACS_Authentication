package com.acs.web.dto;

import lombok.Data;

@Data
public class Login {
	private String command;
	private String response;
	private String apikey;
	private String secretkey;
}

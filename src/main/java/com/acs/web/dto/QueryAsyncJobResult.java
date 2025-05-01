package com.acs.web.dto;

import lombok.Data;

@Data
public class QueryAsyncJobResult {
	
	private String jobid;
	private String command;
	private String apikey;
	private String secretkey;
	private String response;
}

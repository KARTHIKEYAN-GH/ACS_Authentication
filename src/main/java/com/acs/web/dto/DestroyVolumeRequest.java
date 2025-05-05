package com.acs.web.dto;

import lombok.Data;

@Data
public class DestroyVolumeRequest {
	private String id;
	private boolean expunge;
	private String command;
	private String apikey;
	private String secretkey;
	private String response;

}

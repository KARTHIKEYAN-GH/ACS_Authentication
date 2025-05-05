package com.acs.web.dto;

import lombok.Data;

@Data
public class DeleteVolumeRequest {
	private String id; // volumeid
	private String command;
	private String response;
	private String apikey;
	private String secretkey;
}

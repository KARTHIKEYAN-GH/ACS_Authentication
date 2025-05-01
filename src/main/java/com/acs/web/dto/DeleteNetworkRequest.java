package com.acs.web.dto;

import lombok.Data;

@Data
public class DeleteNetworkRequest {
	private String id;   //the ID of the network
	private boolean forced;
	private String command;
	private String apikey;
	private String secretkey;
	private String response;
}

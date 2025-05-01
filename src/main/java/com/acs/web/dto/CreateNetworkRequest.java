package com.acs.web.dto;

import lombok.Data;

@Data
public class CreateNetworkRequest {
	private String zoneId;
	private String name;
	private String networkOfferingId;
	//private String account;
	private String domainid;
	private String command;
	private String apikey;
	private String secretkey;
	private String response;
}

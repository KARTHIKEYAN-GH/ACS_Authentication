package com.acs.web.dto;

import lombok.Data;

@Data
public class CreateVolumeRequest {

	private String name;
	private String command;
	private String diskofferingid;
	private String zoneid;
	private String respone;
	private String apikey;
	private String secretkey; 
}

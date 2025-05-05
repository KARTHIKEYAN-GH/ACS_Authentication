package com.acs.web.dto;

import lombok.Data;

@Data
public class UpdateNetworkDTO {

	private String id; // id of the network to update
	private String name;
	private String sourcenatipaddress;
	private String dns2;
	private String networkofferingid;
	private String userId;
}

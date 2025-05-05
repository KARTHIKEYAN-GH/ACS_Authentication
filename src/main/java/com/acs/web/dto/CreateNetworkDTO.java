package com.acs.web.dto;

import lombok.Data;

@Data
public class CreateNetworkDTO {
	private String zoneId;
	private String name;
	private String networkOfferingId;
	// private String account;
	private String domainid;
}

package com.acs.web.dto;

import lombok.Data;

@Data
public class CreateVolumeDTO {
	private String userId;
	private String name;
	private String diskofferingid;
	private String zoneid;
}

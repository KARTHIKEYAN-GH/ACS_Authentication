package com.acs.web.dto;

import lombok.Data;

@Data
public class DeleteNetworkDTO {
	private String id; // the ID of the network
	private boolean forced;
}

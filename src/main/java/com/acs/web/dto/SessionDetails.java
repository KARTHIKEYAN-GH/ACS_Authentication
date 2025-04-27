package com.acs.web.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SessionDetails implements Serializable {
	private String sessionkey;
	private String jsessionid;
}

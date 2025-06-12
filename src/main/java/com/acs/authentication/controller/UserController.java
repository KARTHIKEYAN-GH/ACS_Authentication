package com.acs.authentication.controller;

import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acs.authentication.entity.User;
import com.acs.authentication.service.AcsService;
import com.acs.authentication.service.UserService;
import com.acs.authentication.util.PasswordCryptoUtil;
import com.acs.web.dto.CreateNetworkDTO;
import com.acs.web.dto.CreateVolumeDTO;
import com.acs.web.dto.DeleteNetworkDTO;
import com.acs.web.dto.GetUserKeysDTO;
import com.acs.web.dto.LoginRequest;
import com.acs.web.dto.UpdateNetworkDTO;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;
@RestController
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "Authorization")
@RequestMapping("/api/user")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@Autowired 
	private PasswordCryptoUtil passwordCryptoUtil;
	
	@PostMapping("/findbyusernameandpassword")
	public String getuser(LoginRequest request) throws Exception {
		String decryptedpassword=passwordCryptoUtil.decrypt(request.getPassword());
		User user=userService.findByUserNameAndPassword(request.getUsername(), decryptedpassword);
		 String id=user.getUserId();
		return id;
	}
}

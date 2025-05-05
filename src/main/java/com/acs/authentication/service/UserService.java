package com.acs.authentication.service;

import com.acs.authentication.entity.User;
import com.acs.web.dto.LoginResponse;

public interface UserService {

	User findByUserNameAndPassword(String userName, String password);

	void save(User user);

	User findByUserId(String userId);

}

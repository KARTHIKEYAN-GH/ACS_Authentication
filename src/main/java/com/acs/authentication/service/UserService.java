package com.acs.authentication.service;

import com.acs.authentication.entity.User;

public interface UserService {

	User findByUserNameAndPassword(String userName, String password);

	void save(User user);

	User findByUserId(String userId);

	User findByUserName(String username);

}

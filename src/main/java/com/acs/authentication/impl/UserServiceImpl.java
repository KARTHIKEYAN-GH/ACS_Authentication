package com.acs.authentication.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.acs.authentication.entity.User;
import com.acs.authentication.repo.UserRepository;
import com.acs.authentication.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public User findByUserNameAndPassword(String userName, String password) {
		return userRepository.findByUserNameAndPassword(userName, password);
	}

	@Override
	public void save(User user) {
		userRepository.save(user);
	}

	@Override
	public User findByUserId(String userId) {
		return userRepository.findByUserId(userId);
	}

	@Override
	public User findByUserName(String username) {
		return userRepository.findByUserName(username);
	}

}

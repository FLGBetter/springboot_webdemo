package springboot_webdemo.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import springboot_webdemo.dao.UserRepository;
import springboot_webdemo.entity.User;

@Service
public class UserService implements IUserService{
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public User findByUsername(String username) {

		return userRepository.findByUsername(username);
	}

	@Override
	public User findByUsernameOrAddress(String username, String address) {

		return userRepository.findByUsernameOrAddress(username, address);
	}

	@Override
	public List<User> findAll() {
		return userRepository.findAll();
	}

}

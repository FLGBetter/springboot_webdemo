package springboot_webdemo.service;

import java.util.List;

import springboot_webdemo.entity.User;

public interface IUserService {
	
	User findByUsername(String username);

	User findByUsernameOrAddress(String username, String address);
	List<User> findAll();
}

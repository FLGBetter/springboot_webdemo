package springboot_webdemo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import springboot_webdemo.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	User findByUsername(String username);

	User findByUsernameOrAddress(String username, String address);
	
	List<User> findAll();
}
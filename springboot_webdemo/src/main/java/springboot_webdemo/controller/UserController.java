package springboot_webdemo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import springboot_webdemo.entity.User;
import springboot_webdemo.factory.ModelBuilders;
import springboot_webdemo.service.IUserService;

@RestController
@Slf4j
public class UserController {
	
	@Autowired
	private IUserService userService;
	
	@RequestMapping("user/getUserInfo")
	public User getUserInfo() {
		try {
			User user = (User)ModelBuilders.bulid(User.class);
			user.setUsername("朱XPHB明");
			user.setPassword("AG8xph0b271");
			user.setAddress("世纪汇广场");
			log.info("user={}",user.toString());
			return user;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping("user/getUser")
	public User getUserByName(@PathVariable String username) {
		try {
			User user = userService.findByUsername(username);
			log.info("user={}",user.toString());
			return user;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping("user/getAllUser")
	public List<User> getAllUser() {
		try {
			List<User> userlist = userService.findAll();
			log.info("user={}",userlist.size());
			return userlist;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

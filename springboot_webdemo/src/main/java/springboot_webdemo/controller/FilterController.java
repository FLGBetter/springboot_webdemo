package springboot_webdemo.controller;

import java.util.Map;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class FilterController {
	
	@Autowired
//	@Qualifier("interfaceCostFilterByAnnoFilterRegist")
    FilterRegistrationBean registration;
	
	@RequestMapping("filter/getFilterParam")
	public Map<String,String> getUserInfo() {
		try {
			Map<String,String> map = registration.getInitParameters();
//			Filter filter = registration.getFilter();
			String nickname = (String) map.get("nickname");
			log.info("nickname={}",nickname);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

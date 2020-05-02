package springboot_webdemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import springboot_webdemo.entity.CommonProperties;

@Slf4j
@RestController
public class PropertiesController {
	
	@Autowired
	private CommonProperties commonProperties;
	
	
	@RequestMapping("/getProperties")
	public CommonProperties getCommonProperties(){
		log.info("commonProperties={}",commonProperties.getTitle());
		return commonProperties;
	}
}

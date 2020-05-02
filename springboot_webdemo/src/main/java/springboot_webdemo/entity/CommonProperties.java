package springboot_webdemo.entity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class CommonProperties {
	
	@Value("${com.lf.title}")
	private String title;
	
	@Value("${com.lf.description}")
	private String description;
	
	
}

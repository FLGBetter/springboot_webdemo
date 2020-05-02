package springboot_webdemo.config;

import javax.servlet.Filter;

import org.apache.catalina.filters.RemoteIpFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import springboot_webdemo.filter.InterfaceCostFilter;
import springboot_webdemo.filter.InterfaceCostFilterByAnno;

@Configuration
public class FilterConfig {
	
	//应用程序运行在一台负载均衡代理服务器后方，因此需要将代理服务器发来的请求包含的IP地址转换成真正的用户IP。
	//Tomcat 8 提供了对应的过滤器：RemoteIpFilter
	@Bean
	public RemoteIpFilter remoteIpFilter(){
		return new RemoteIpFilter();
	}
	
//	@Bean("interfaceCostFilterFilterRegist")
//	@Primary
	@Bean
	public FilterRegistrationBean<InterfaceCostFilter> getInterfaceCostFilter(){
		
		FilterRegistrationBean registrationBean = new FilterRegistrationBean ();
		registrationBean.setFilter(new InterfaceCostFilter());
		registrationBean.addUrlPatterns("/*");
		registrationBean.addInitParameter("nickname", "王小二");//添加默认参数
		registrationBean.setName("InterfaceCostFilter");
		registrationBean.setOrder(2);
		return registrationBean;
	}
	
	
//	@Bean("interfaceCostFilterByAnnoFilterRegist")
	@Primary
	@Bean
	public FilterRegistrationBean<Filter> interfaceCostFilterByAnno(){
		
		FilterRegistrationBean registrationBean = new FilterRegistrationBean ();
		registrationBean.setFilter(new InterfaceCostFilterByAnno());
		registrationBean.addUrlPatterns("/*");
		registrationBean.addInitParameter("nickname", "王小二");//添加默认参数
		registrationBean.addInitParameter("age", "张三");//添加默认参数
		registrationBean.setName("interfaceCostFilterByAnno");
		registrationBean.setOrder(1);
		return registrationBean;
	}
}

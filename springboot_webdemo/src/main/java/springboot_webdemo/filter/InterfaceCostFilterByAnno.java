package springboot_webdemo.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

//@WebFilter(urlPatterns="/*",filterName="InterfaceCostFilterByAnno",initParams={@WebInitParam(name="age",value="21"),
//										@WebInitParam(name = "charSet", value = "utf-8")})
//@Component

//@Order(value=1)
public class InterfaceCostFilterByAnno implements Filter{

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		long benginTime = System.currentTimeMillis();
		chain.doFilter(request, response);
		long endTime = System.currentTimeMillis();
		long cost = endTime - benginTime;
		System.out.println("InterfaceCostFilterByAnno记录本次调用"+"服务花费时间："+cost+"ms");
	}

	@Override
	public void destroy() {
		
	}

}

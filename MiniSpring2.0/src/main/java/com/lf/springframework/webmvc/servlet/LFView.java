package com.lf.springframework.webmvc.servlet;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LFView {
	
	public static final String DEFAULT_CONTENT_TYPE = "text/html;charset=utf-8";
	private File viewFile;
	
	public LFView(File viewFile){
		this.viewFile = viewFile;
	}
	
	public String getContentType(){
		return DEFAULT_CONTENT_TYPE;
	}
	public void render(Map<String, ?> model,HttpServletRequest request, HttpServletResponse response) throws Exception{
		StringBuffer sb = new StringBuffer();
		RandomAccessFile ra = new RandomAccessFile(this.viewFile,"r");
		try {
			String line = null; 
			while (null != (line = ra.readLine())) {
				line = new String(line.getBytes("ISO-8859-1"),"utf-8");
				Pattern pattern = Pattern.compile("￥\\{[^\\}]+\\}",Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					String paramName = matcher.group();
					paramName = paramName.replaceAll("￥\\{|\\}",""); 
					Object paramValue = model.get(paramName);
					if (null == paramValue) { continue; }
					//要把￥{}中间的这个字符串给取出来
					line = matcher.replaceFirst(makeStringForRegExp(paramValue.toString()));
					matcher = pattern.matcher(line);
				}
				sb.append(line);
			}
		}finally {
			ra.close();
		}
		response.setCharacterEncoding("utf-8"); 
		//response.setContentType(DEFAULT_CONTENT_TYPE); 
		response.getWriter().write(sb.toString());
	}
	
	//处理特殊字符 
	public static String makeStringForRegExp(String str) {
		
		return str.replace("\\", "\\\\").replace("&", "\\&")
				  .replace("*", "\\*") .replace("+", "\\+")
				  .replace("|", "\\|") .replace("{", "\\{")
				  .replace("}", "\\}") .replace("(", "\\(")
				  .replace(")", "\\)") .replace("^", "\\^")
				  .replace("$", "\\$") .replace("[", "\\[")
				  .replace("]", "\\]") .replace("?", "\\?")
				  .replace(",", "\\,") .replace(".", "\\.");
	}
}

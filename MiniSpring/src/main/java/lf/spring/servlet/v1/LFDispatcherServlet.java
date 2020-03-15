package lf.spring.servlet.v1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lf.spring.annotation.LFAutowired;
import lf.spring.annotation.LFController;
import lf.spring.annotation.LFRequestMapping;
import lf.spring.annotation.LFService;
/**
 * v1
 */
public class LFDispatcherServlet extends HttpServlet{
	
	//保存application.properties的内容
	private Properties contextConfig = new Properties();
	
	//保存扫描到的所有class的类全路径名称,后面根据Class.forName()反射生成对象
	private List<String> classNames = new ArrayList<String>();
	
	//IOC容器,暂时不用ConcurrentHashMap,主要关注思想
	Map<String,Object> ioc = new HashMap<String,Object>();
	
	//保存url和method 的对应关系
	Map<String,Method> handlerMapping = new HashMap<String,Method>();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		doPost(req,resp);
	}
	
	//运行阶段
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
            doDispatch(req,resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
        }
	}
	
	//调用
	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
		//绝对路径
		String url = req.getRequestURI();
		StringBuffer requestURL = req.getRequestURL();
		//处理成相对路径
		String contextPath = req.getContextPath();
		url = url.replaceAll(contextPath, "").replaceAll("/+", "/");
		
		if(!handlerMapping.containsKey(url)){
			resp.getWriter().write("404 Not Found!!!");
			return ;
		}
		
		Method method = this.handlerMapping.get(url);
		//投机取巧,反射获取class名称
		String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
		//投机取巧,暂时参数写死
		Map<String,String[]> parameters = req.getParameterMap();
//		method.invoke(ioc.get(beanName),new Object[]{req,resp, parameters.get("name")[0]});
		method.invoke(ioc.get(beanName),req,resp, parameters.get("name")[0]);
	}

	//初始化阶段
	@Override
	public void init(ServletConfig config) throws ServletException {
		//1.加载配置文件
		doLoadConfig(config.getInitParameter("contextConfigLocation"));
		
		//2.扫描相关的类
		doScanner((String)contextConfig.get("scanPackage"));
		
		//3.初始化类，并放到IOC容器中
		doInstance();
		
		//4.完成依赖注入
		doAutowired();
		
		//5.初始化HandlerMapping
		initHandleMapping();
		
		System.out.println("LF MiniSpring Inited!");
	}
	
	//初始化HandlerMapping,即url和method一一对应关系
	private void initHandleMapping() {
		if(ioc.isEmpty()){ return; }
		for (Entry<String, Object> entry : ioc.entrySet()) {
			Class<? extends Object> clazz = entry.getValue().getClass();
			if(!clazz.isAnnotationPresent(LFController.class)){ continue;}
			
			//保存写在类上面的url @LFRequestMapping("/demo")
			String baseUrl = "";
			if(clazz.isAnnotationPresent(LFRequestMapping.class)){
				LFRequestMapping requestMapping = clazz.getAnnotation(LFRequestMapping.class);
				baseUrl = requestMapping.value();
			}
			
			//获取所有的公共方法
			for (Method method : clazz.getMethods()) {
				if(!method.isAnnotationPresent(LFRequestMapping.class)){ continue;}
				LFRequestMapping requestMapping = method.getAnnotation(LFRequestMapping.class);
				//优化
				///demo/query/
				String url = ("/"+ baseUrl+ requestMapping.value()).replaceAll("/+", "/");
				handlerMapping.put(url, method);
				System.out.println("map:"+url+"->"+method);
			}
		}
	}

	//依赖注入
	private void doAutowired() {
		
		if(ioc.isEmpty()){return;}
		for (Entry<String, Object> entry : ioc.entrySet()) {
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {
				if(!field.isAnnotationPresent(LFAutowired.class)){continue;}
				LFAutowired autowired = field.getAnnotation(LFAutowired.class);
				//如果用户没有自定义beanName，默认就根据类型注入
				//这里省去了beanName类名首字母小写的判断，如果是大写，则首字母转成小写
				String beanName = autowired.value().trim();
				if("".equals(beanName)){
					//获取接口类名，根据接口类名去找
					beanName = field.getType().getSimpleName();
				}
				try {
					//用反射给属性赋值    需要赋值的对象             属性赋的值
					field.setAccessible(true);
					field.set(entry.getValue(), ioc.get(beanName));
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}
	}

	//初始化类，并放到IOC容器中
	private void doInstance() {
		//初识化，为DI做准备
		if(classNames.isEmpty()){return;}
		
		try {
			for (String className : classNames) {
				Class<?> clazz = Class.forName(className);
				//思考什么样的类需要初始化？
				//加了注解的类才需要初始化
				if(clazz.isAnnotationPresent(LFController.class)){
					Object instance = clazz.newInstance();
					//spring默认类名首字母小写
					String beanName = clazz.getSimpleName();
					//首字母转小写
					beanName = toLowerFirstCase(beanName);
					//加入ioc容器
					ioc.put(beanName, instance);
				}else if(clazz.isAnnotationPresent(LFService.class)){
					//1自定义beanName  @LFService("ddService")
					LFService service = clazz.getAnnotation(LFService.class);
					String beanName = service.value();
					//2..默认类名首字母小写
					if("".equals(beanName)){
						beanName = toLowerFirstCase(clazz.getSimpleName());
					}
					//3.根据类型自动赋值(注入的值是接口，接口本身不能实例化),投机取巧的方式
					Object instance = clazz.newInstance();
					ioc.put(beanName, instance);
					for (Class<?> i: clazz.getInterfaces()) {
						if(ioc.containsKey(i.getSimpleName())){
							throw new RuntimeException("the "+i.getName()+" is exists!");
						}
						//将接口类名作为key
						ioc.put(i.getSimpleName(), instance);
					}
				}else{continue;}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}
		
	}
	
	//首字母转小写
	private String toLowerFirstCase(String beanName) {
		char[] chars = beanName.toCharArray();
		//判断首字母是否是大写，是加32变小写
		if(!(chars[0]>106)){return String.valueOf(chars);}
		chars[0] += 32;
		return String.valueOf(chars);
	}

	//扫描相关的类
	private void doScanner(String scanPackage) {
		//scanPackage是包路径，==com.lf
		//替换成文件路径 /com/if
		//classpath
		//.的转义字符
		URL url = this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.", "/"));
		File classpath = new File(url.getFile());
		File[] listFiles = classpath.listFiles();
		for (File file : listFiles) {
			if(file.isDirectory()){
				doScanner(scanPackage+"."+file.getName());
			}else{
				if(!file.getName().endsWith(".class")){continue;}
				String className = scanPackage+"."+file.getName().replaceAll(".class", "");
				classNames.add(className);
			}
		}
	}
	

	//加载配置
	private void doLoadConfig(String contextConfigLocation) {
		
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
		try {
			contextConfig.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	
}

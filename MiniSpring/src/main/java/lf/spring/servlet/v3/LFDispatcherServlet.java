package lf.spring.servlet.v3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lf.spring.annotation.LFAutowired;
import lf.spring.annotation.LFController;
import lf.spring.annotation.LFRequestMapping;
import lf.spring.annotation.LFRequestParam;
import lf.spring.annotation.LFService;
/**
 * 
 * V3优化，方法中不需要req和resp，保持方法的职责单一，解耦合；
 * 把url放入handler中并requestMapping映射正则路径问题query.*
 * 
 *
 */
public class LFDispatcherServlet extends HttpServlet{
	
	//保存application.properties的内容
	private Properties contextConfig = new Properties();
	
	//保存扫描到的所有class的类名称
	private List<String> classNames = new ArrayList<String>();
	
	//IOC容器,暂时不用ConcurrentHashMap,主要关注思想
	Map<String,Object> ioc = new HashMap<String,Object>();
	
	//保存url和method 的对应关系
	private List<Handler> handlerMapping = new ArrayList<Handler>();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		doPost(req,resp);
	}
	
	//运行阶段
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			//调用doDispatch处理请求
            doDispatch(req,resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
        }
	}
	
	//调用doDispatch处理请求
	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
		//匹配到handler
		Handler handler = getHandler(req);
		
		if(handler == null){
			//如果没有匹配上，返回404错误
			resp.getWriter().write("404 Not Found");
			return;
		}
		
		//不用使用反射拿到参数，直接从handler对象中拿到
		//获取方法的参数列表
		Class<?> [] paramTypes = handler.method.getParameterTypes();
		
		//保存所有需要自动赋值的参数值
		Object [] paramValues = new Object[paramTypes.length];
		
		Map<String,String[]> params = req.getParameterMap();
		for (Entry<String, String[]> param : params.entrySet()) {
			String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
			
			//如果找到匹配的对象，则开始填充参数值
			if(!handler.paramIndexMapping.containsKey(param.getKey())){continue;}
			int index = handler.paramIndexMapping.get(param.getKey());
			paramValues[index] = convert(paramTypes[index],value);
		}
		//设置方法中的request和response对象
		if(handler.paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
			int reqIndex = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
			paramValues[reqIndex] = req;
		}
		
		if(handler.paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
			int respIndex = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
			paramValues[respIndex] = resp;
		}
		
		Object returnValue = handler.method.invoke(handler.controller, paramValues);
		if(returnValue == null || returnValue instanceof Void){return ;}
		resp.getWriter().write(returnValue.toString());
	}

	//url传过来的参数都是String类型的，HTTP是基于字符串协议
	//只需要把String转换为任意类型就好
	private Object convert(Class<?> type,String value){
		if(Integer.class == type){
			return Integer.valueOf(value);
		}
		//如果还有double或者其他类型，继续加if
		//这时候，我们应该想到策略模式了
		//在这里暂时不实现，希望小伙伴自己来实现
		return value;
	}

	private Handler getHandler(HttpServletRequest req) throws Exception{
		if(handlerMapping.isEmpty()){ return null; }
		//绝对路径
		String url = req.getRequestURI();
		//处理成相对路径
		String contextPath = req.getContextPath();
		url = url.replace(contextPath, "").replaceAll("/+", "/");
		
		for (Handler handler : handlerMapping) {
			try{
				Matcher matcher = handler.pattern.matcher(url);
				//如果没有匹配上继续下一个匹配
				if(!matcher.matches()){ continue; }
				
				return handler;
			}catch(Exception e){
				throw e;
			}
		}
		return null;
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
			String url = "";
			if(clazz.isAnnotationPresent(LFRequestMapping.class)){
				LFRequestMapping requestMapping = clazz.getAnnotation(LFRequestMapping.class);
				url = requestMapping.value();
			}
			
			//获取所有的公共方法
			for (Method method : clazz.getMethods()) {
				if(!method.isAnnotationPresent(LFRequestMapping.class)){ continue;}
				LFRequestMapping requestMapping = method.getAnnotation(LFRequestMapping.class);
				//优化
				///demo/query/
				String regex = ("/" + url +"/"+ requestMapping.value()+".*").replaceAll("/+", "/");
				//根据正则匹配/lf/query.*
				Pattern pattern = Pattern.compile(regex);
				handlerMapping.add(new Handler(pattern,entry.getValue(),method));
				System.out.println("mapping " + regex + "," + method);
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

	//初识化，为DI做准备
	private void doInstance() {
		
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

	/**
	 * Handler记录Controller中的RequestMapping和Method的对应关系
	 * 内部类
	 */
	private class Handler{
		
		protected Object controller;	//保存方法对应的实例
		protected Method method;		//保存映射的方法
		protected Pattern pattern;		//正则匹配路径
		
		protected Map<String,Integer> paramIndexMapping;	//记录参数顺序，方便将实参传递好顺序，调用
		
		/**
		 * 构造一个Handler基本的参数
		 * @param controller
		 * @param method
		 */
		protected Handler(Pattern pattern,Object controller,Method method){
			this.controller = controller;
			this.method = method;
			this.pattern = pattern;
			
			paramIndexMapping = new HashMap<String,Integer>();
			putParamIndexMapping(method);
		}
		
		//保存参数位置
		private void putParamIndexMapping(Method method){
			
			//提取方法中加了注解的参数
			//二维数组接收，第一[]带表行，[]代表列
			Annotation [] [] pa = method.getParameterAnnotations();
			for (int i = 0; i < pa.length ; i ++) {
				for(Annotation a : pa[i]){//每行数据又是一个注解数组
					if(a instanceof LFRequestParam){
						String paramName = ((LFRequestParam) a).value();
						if(!"".equals(paramName.trim())){
							paramIndexMapping.put(paramName, i);
						}
					}
				}
			}
			
			//提取方法中的request和response参数
			Class<?> [] paramsTypes = method.getParameterTypes();
			for (int i = 0; i < paramsTypes.length ; i ++) {
				Class<?> type = paramsTypes[i];
				if(type == HttpServletRequest.class ||
				   type == HttpServletResponse.class){
					paramIndexMapping.put(type.getName(),i);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		Pattern pattern = Pattern.compile("/lf/query.*");
		Matcher matcher = pattern.matcher("/lf/queryTom");
		System.out.println(matcher.matches());
		boolean b = Pattern.matches("lfquery", "lfqueryTom");
		System.out.println(b);
	}
}

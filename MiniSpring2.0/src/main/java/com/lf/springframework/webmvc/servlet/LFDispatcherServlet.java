package com.lf.springframework.webmvc.servlet;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.lf.springframework.annotation.LFAutowired;
import com.lf.springframework.annotation.LFController;
import com.lf.springframework.annotation.LFRequestMapping;
import com.lf.springframework.annotation.LFService;
import com.lf.springframework.context.LFApplicationContext;

import lombok.extern.slf4j.Slf4j;

/**
 * DispatcherServlet
 */
@Slf4j
public class LFDispatcherServlet extends HttpServlet {

	private static final String CONTEXT_CONFIGLOCATION = "contextConfigLocation";

	private LFApplicationContext context = null;

	// 保存handlerMapping,handlerMapping里保存url和method 的对应关系
	private List<LFHandlerMapping> handlerMappings = new ArrayList<LFHandlerMapping>();

	// handlerAdapters
	Map<LFHandlerMapping, LFHandlerAdapter> handlerAdapters = new HashMap<LFHandlerMapping, LFHandlerAdapter>();

	// 保存
	private List<LFViewResolver> viewResolvers = new ArrayList<LFViewResolver>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		doPost(req, resp);
	}

	// 运行阶段
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			doDispatch(req, resp);
		} catch (Exception e) {
			resp.getWriter()
					.write("<font size='25' color='blue'>500 Exception</font><br/>Details:<br/>"
							+ Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "").replaceAll("\\s", "\r\n")
							+ "<font color='green'><i>Copyright@LFEDU</i></font>");
			e.printStackTrace();
			// resp.getWriter().write("500 Exception " +
			// Arrays.toString(e.getStackTrace()));
		}
	}

	// 调用
	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

		// 1.根据url匹配到handlerMapping
		LFHandlerMapping handler = getHandler(req);

		if (handler == null) {
			// 如果没有匹配上，返回404错误
			processDispatchResult(req, resp, new LFModelAndView("404"));
			return;
		}

		// 2.准备调用前参数
		LFHandlerAdapter ha = getHandlerAdapter(handler);

		// 3.真正调用方法，返回ModelAndView(存储了返回页面上的值和页面名称)
		LFModelAndView mv = ha.handle(req, resp, handler);

		// 4.输出
		processDispatchResult(req, resp, mv);
	}

	// 初始化阶段
	@Override
	public void init(ServletConfig config) throws ServletException {
		// 1.初始化ApplicationContext
		context = new LFApplicationContext(config.getInitParameter(CONTEXT_CONFIGLOCATION));

		// 2.初始化SpringMvc九大组件
		initStrategies(context);

	}

	// 2.初始化SpringMvc九大组件
	void initStrategies(LFApplicationContext context) {
		// 有九种策略
		// 针对于每个用户请求，都会经过一些处理的策略之后，最终才能有结果输出
		// 每种策略可以自定义干预，但是最终的结果都是一致
		// ============= 这里说的就是传说中的九大组件 ================
		initMultipartResolver(context);// 文件上传解析，如果请求类型是 multipart 将通过
										// MultipartResolver 进行文件上传解析

		initLocaleResolver(context);// 本地化解析
		initThemeResolver(context); // 主题解析

		/** 自己实现 */
		// LFHandlerMapping 用来保存 Controller 中配置的 RequestMapping 和 Method 的一个对应关系
		initHandlerMappings(context);// 通过 HandlerMapping，将请求映射到处理器

		/** 自己实现 */
		// HandlerAdapters 用来动态匹配 Method 参数，包括类转换，动态赋值
		initHandlerAdapters(context);// 通过 HandlerAdapter 进行多类型的参数动态匹配

		initHandlerExceptionResolvers(context);// 如果执行过程中遇到异常，将交给HandlerExceptionResolver
												// 来解析
		initRequestToViewNameTranslator(context);// 直接解析请求到视图名
		/** 自己实现 */
		// 通过 ViewResolvers 实现动态模板的解析
		// 自己解析一套模板语言
		initViewResolvers(context);// 通过 viewResolver 解析逻辑视图到具体视图实现

		initFlashMapManager(context);// flash 映射管理器
	}

	// 准备调用前参数
	private LFHandlerAdapter getHandlerAdapter(LFHandlerMapping handler) {

		if (this.handlerAdapters.isEmpty()) {
			return null;
		}
		LFHandlerAdapter ha = this.handlerAdapters.get(handler);
		if (ha.supports(handler)) {
			return ha;
		}
		return null;
	}

	private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, LFModelAndView mv) throws Exception {
		// 把给我的ModelAndView变成一个html、outputStream、freemaker、json等
		// contextType
		// 调用 viewResolver 的 resolveView 方法
		if (null == mv) {
			return;
		}
		if (this.viewResolvers.isEmpty()) {
			return;
		}
		if (this.viewResolvers != null) {
			for (LFViewResolver viewResolver : this.viewResolvers) {
				LFView view = viewResolver.resolveViewName(mv.getViewName(), null);
				if (view != null) {
					view.render(mv.getModel(), req, resp);
					return;
				}
			}
		}
	}

	// 通过 ViewResolvers 实现动态模板的解析
	private void initViewResolvers(LFApplicationContext context) {
		// 在页面敲一个 http://localhost/first.html
		// 解决页面名字和模板文件关联的问题
		String templateRoot = context.getConfig().getProperty("templateRoot");
		URL url = this.getClass().getClassLoader().getResource(templateRoot);
		File classpath = new File(url.getFile());
		File[] listFiles = classpath.listFiles();
		for (int i = 0; i < listFiles.length; i++) {
			this.viewResolvers.add(new LFViewResolver(templateRoot));
		}

	}

	// HandlerAdapters 用来动态匹配 Method 参数，包括类转换，动态赋值
	// 在初始化阶段，我们能做的就是，将这些参数的名字或者类型按一定的顺序保存下来
	// 因为后面用反射调用的时候，传的形参是一个数组
	// 可以通过记录这些参数的位置 index,挨个从数组中填值，这样的话，就和参数的顺序无关了
	private void initHandlerAdapters(LFApplicationContext context) {

		for (LFHandlerMapping handlerMapping : this.handlerMappings) {
			// 每一个方法有一个参数列表，那么这里保存的是形参列表
			this.handlerAdapters.put(handlerMapping, new LFHandlerAdapter());
		}

	}

	// 用来保存 Controller 中配置的 RequestMapping 和 Method 的一个对应关系
	private void initHandlerMappings(LFApplicationContext context) {

		// 拿到所有beanName
		String[] beanNames = context.getBeanDefinitionNames();
		try {

			for (String beanName : beanNames) {
				Object controller = context.getBean(beanName);// 根据beanName获取到bean
				Class<? extends Object> clazz = controller.getClass();

				// 判断是否是Controller
				if (!clazz.isAnnotationPresent(LFController.class)) {
					continue;
				}

				// 保存写在类上面的url @LFRequestMapping("/demo")
				String url = "";
				if (clazz.isAnnotationPresent(LFRequestMapping.class)) {
					LFRequestMapping requestMapping = clazz.getAnnotation(LFRequestMapping.class);
					url = requestMapping.value();
				}

				// 获取所有的公共方法
				for (Method method : clazz.getMethods()) {
					if (!method.isAnnotationPresent(LFRequestMapping.class)) {
						continue;
					}
					LFRequestMapping requestMapping = method.getAnnotation(LFRequestMapping.class);
					// 优化
					/// demo/query/
					String regex = ("/" + url + "/" + requestMapping.value() + ".*").replaceAll("/+", "/");
					// 根据正则匹配/lf/query.*
					Pattern pattern = Pattern.compile(regex);
					this.handlerMappings.add(new LFHandlerMapping(pattern, controller, method));
					log.info("mapping " + regex + "," + method);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void initFlashMapManager(LFApplicationContext context) {
	}

	private void initRequestToViewNameTranslator(LFApplicationContext context) {
	}

	private void initHandlerExceptionResolvers(LFApplicationContext context) {
	}

	private void initThemeResolver(LFApplicationContext context) {
	}

	private void initLocaleResolver(LFApplicationContext context) {
	}

	private void initMultipartResolver(LFApplicationContext context) {
	}

	// 根据url匹配到handlerMapping
	private LFHandlerMapping getHandler(HttpServletRequest req) throws Exception {
		if (this.handlerMappings.isEmpty()) {
			return null;
		}
		// 绝对路径
		String url = req.getRequestURI();
		// 处理成相对路径
		String contextPath = req.getContextPath();
		url = url.replace(contextPath, "").replaceAll("/+", "/");

		for (LFHandlerMapping handler : handlerMappings) {
			try {
				Matcher matcher = handler.getPattern().matcher(url);
				// 如果没有匹配上继续下一个匹配
				if (!matcher.matches()) {
					continue;
				}

				return handler;
			} catch (Exception e) {
				throw e;
			}
		}
		return null;
	}
}

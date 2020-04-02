package com.lf.springframework.beans.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.lf.springframework.beans.config.LFBeanDefinition;

public class LFBeanDefinitionReader {

	// 保存application.properties的内容
	private Properties contextConfig = new Properties();
	private final String SCAN_PACKAGE = "scanPackage";
	// 保存扫描到的所有class的类名称
	private List<String> registyBeanClasses = new ArrayList<String>();

	// 加载配置
	public LFBeanDefinitionReader(String... locations) {
		// 通过URL找到文件并加载
		doLoadConfig(locations);
		// 扫描相关的类
		doScanner(contextConfig.getProperty(SCAN_PACKAGE));
	}

	// 封装成beanDefinition
	public List<LFBeanDefinition> loadBeanDefinitions() {

		List<LFBeanDefinition> result = new ArrayList<LFBeanDefinition>();
		try {
			for (String className : registyBeanClasses) {
				Class<?> beanClass = Class.forName(className);
				if (beanClass.isInterface()) {
					continue;
				}
				// 把每一个配置信息解析成一个BeanDefinition
				LFBeanDefinition beanDefinition = doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()),
						beanClass.getName());
				result.add(beanDefinition);

				// beanName有三种情况
				// 1自定义beanName @LFService("ddService")
				// 2.默认类名首字母小写
				// 3.接口注入
				Class<?>[] interfaces = beanClass.getInterfaces();
				for (Class<?> i : interfaces) {
					// 多个实现类，只能覆盖
//					result.add(doCreateBeanDefinition(toLowerFirstCase(i.getSimpleName()), beanClass.getName()));
					result.add(doCreateBeanDefinition(i.getName(), beanClass.getName()));
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	// 通过URL找到文件并加载
	private void doLoadConfig(String... locations) {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(locations[0].replaceAll("classpath:", ""));
		try {
			contextConfig.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 扫描相关的类
	private void doScanner(String scanPackage) {
		// scanPackage是包路径，==com.lf
		// 替换成文件路径 /com/if
		// classpath
		// .的转义字符
//		URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
		//路径转换问题，非web下获取路径方式如下
		URL url = this.getClass().getResource("/" + scanPackage.replaceAll("\\.", "/"));
		File classpath = new File(url.getFile());
		File[] listFiles = classpath.listFiles();
		for (File file : listFiles) {
			if (file.isDirectory()) {
				doScanner(scanPackage + "." + file.getName());
			} else {
				if (!file.getName().endsWith(".class")) {
					continue;
				}
				String className = scanPackage + "." + file.getName().replaceAll(".class", "");
				registyBeanClasses.add(className);
			}
		}
	}

	// 获取配置
	public Properties getConfig() {

		return this.contextConfig;
	}

	// 把每一个配置信息解析成一个BeanDefinition
	private LFBeanDefinition doCreateBeanDefinition(String beanName, String className) {

		LFBeanDefinition beanDefinition = new LFBeanDefinition();
		beanDefinition.setBeanClassName(className);
		beanDefinition.setFactoryBeanName(beanName);
		return beanDefinition;
	}

	// 首字母转小写
	private String toLowerFirstCase(String beanName) {
		char[] chars = beanName.toCharArray();
		// 判断首字母是否是大写，是加32变小写
		if ((chars[0] >= 97)) {
			return String.valueOf(chars);
		}
		chars[0] += 32;
		return String.valueOf(chars);
	}

}

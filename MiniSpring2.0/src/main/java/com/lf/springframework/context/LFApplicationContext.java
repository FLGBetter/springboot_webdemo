package com.lf.springframework.context;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.lf.springframework.annotation.LFAutowired;
import com.lf.springframework.annotation.LFController;
import com.lf.springframework.annotation.LFService;
import com.lf.springframework.aop.LFAopProxy;
import com.lf.springframework.aop.LFCglibAopProxy;
import com.lf.springframework.aop.LFJdkDynamicAopProxy;
import com.lf.springframework.aop.config.LFAopConfig;
import com.lf.springframework.aop.support.LFAdvisedSupport;
import com.lf.springframework.beans.LFBeanWrapper;
import com.lf.springframework.beans.config.LFBeanDefinition;
import com.lf.springframework.beans.config.LFBeanPostProcessor;
import com.lf.springframework.beans.support.LFBeanDefinitionReader;
import com.lf.springframework.beans.support.LFDefaultListableBeanFactory;
import com.lf.springframework.core.LFBeanFactory;



/**
 * @author LF
 *
 */
public class LFApplicationContext extends LFDefaultListableBeanFactory implements LFBeanFactory{
	
	//配置项
	private String[] configLocations;
	//BeanDefinition解析器
	private LFBeanDefinitionReader reader;
	//单例IOC容器缓存
	public final Map<String,Object> factoryBeanObjectCache = new ConcurrentHashMap<String,Object>(256);
	//通用IOC容器缓存
	public final Map<String,LFBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<String,LFBeanWrapper>(256);
	
	public LFApplicationContext(String... configLocations){
		this.configLocations = configLocations;
		
		refresh();
	}
	
	@Override
	public void refresh() {
		
		//1.定位，定位配置文件
		reader = new LFBeanDefinitionReader(this.configLocations);
		
		//2.加载，加载配置文件，扫描相关的类，把他们封装成BeanDefinition
		List<LFBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
		
		//3.注册,把配置信息放入容器中（伪IOC容器）
		doRegisterBeanDefinitions(beanDefinitions);
		
		//4.非延迟加载的类进行初始化
		doAutowrited();
	}

	//非延迟加载的类初始化
	private void doAutowrited() {
		
		for (Map.Entry<String, LFBeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()) {
			String beanName = beanDefinitionEntry.getKey();
			if(!beanDefinitionEntry.getValue().isLazyInit()){
				try {
					getBean(beanName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	//把配置信息放入容器中（伪IOC容器）
	private void doRegisterBeanDefinitions(List<LFBeanDefinition> beanDefinitions) {

		for (LFBeanDefinition beanDefinition : beanDefinitions) {
			//判断初始化时是否重复bean
//			 if(super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
//	                throw new Exception("The “" + beanDefinition.getFactoryBeanName() + "” is exists!!");
//	            }
			super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
		}
	}

	@Override
	public Object getBean(String beanName) throws Exception {
		
		LFBeanDefinition lfBeanDefinition = this.beanDefinitionMap.get(beanName);
        Object instance = null;

        //这个逻辑还不严谨，自己可以去参考Spring源码
        //工厂模式 + 策略模式,bean初始化前
        LFBeanPostProcessor postProcessor = new LFBeanPostProcessor();

        postProcessor.postProcessBeforeInitialization(instance,beanName);
		//两个方法，解决循环依赖
		//1.初始化
		LFBeanWrapper beanWrapper = instantiateBean(beanName,lfBeanDefinition);
		
		
		//2.把beanWrapper存到IOC容器中
		this.factoryBeanInstanceCache.put(beanName, beanWrapper);
		
		//bean初始化后需要的操作
		postProcessor.postProcessBeforeInitialization(instance,beanName);
		//3.注入
		populateBean(beanName,new LFBeanDefinition(), beanWrapper);
		return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
	}
	
	//注入
	private void populateBean(String beanName, LFBeanDefinition lfBeanDefinition, LFBeanWrapper lfBeanWrapper) {
		Object instance = lfBeanWrapper.getWrappedInstance();
		//判断加了注解的类，才执行注入
		Class<?> clazz = lfBeanWrapper.getWrappedClass();
		if(!(clazz.isAnnotationPresent(LFController.class) || clazz.isAnnotationPresent(LFService.class))){ return;}
		
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if(!field.isAnnotationPresent(LFAutowired.class)){continue;}
			LFAutowired autowired = field.getAnnotation(LFAutowired.class);
			//如果用户没有自定义beanName，默认就根据类型注入
			//这里省去了beanName类名首字母小写的判断，如果是大写，则首字母转成小写
			String autowiredBeanName = autowired.value().trim();
			if("".equals(autowiredBeanName)){
				//获取接口类名，根据接口类名去找
				autowiredBeanName = field.getType().getName();
			}
			try {
				//用反射给属性赋值    需要赋值的对象             属性赋的值
				field.setAccessible(true);
				//factoryBeanInstanceCache不一定有值
				if(this.factoryBeanInstanceCache.containsKey(autowiredBeanName)){
					field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
				}
//				if(this.factoryBeanInstanceCache.get(autowiredBeanName) == null){ 
//					Object newInstance = clazz.newInstance();
//					LFBeanWrapper beanWrapper = new LFBeanWrapper(newInstance);
//					factoryBeanInstanceCache.put(autowiredBeanName,beanWrapper) ;
//				}
//                field.set(instance,this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}

	//初始化
	private LFBeanWrapper instantiateBean(String beanName, LFBeanDefinition lfBeanDefinition) {

		//1.拿到实例化对象的类名
		String className = lfBeanDefinition.getBeanClassName();
		Object instance = null;
		try {
			//2.反射实例化
			//假设默认是单例,细节暂时忽略，先写主线
			if(factoryBeanObjectCache.containsKey(className)){
				instance = this.factoryBeanObjectCache.get(className);
			}else{
				Class<?> clazz = Class.forName(className);
				instance = clazz.newInstance();
				
				LFAdvisedSupport config = instantionAopConfig(lfBeanDefinition);
				config.setTargetClass(clazz);
				config.setTarget(instance);
				if(config.pointCutMatch()) {
					instance = createProxy(config).getProxy();
				}
				
				this.factoryBeanObjectCache.put(className, instance);
				this.factoryBeanObjectCache.put(lfBeanDefinition.getFactoryBeanName(), instance);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//3.封装对象到beanWrapper中
		LFBeanWrapper beanWrapper = new LFBeanWrapper(instance);
			//factoryBeanInstanceCache
		return beanWrapper;
	}
	
	 private LFAdvisedSupport instantionAopConfig(LFBeanDefinition LFBeanDefinition) {
	        LFAopConfig config = new LFAopConfig();
	        config.setPointCut(this.reader.getConfig().getProperty("pointCut"));
	        config.setAspectClass(this.reader.getConfig().getProperty("aspectClass"));
	        config.setAspectBefore(this.reader.getConfig().getProperty("aspectBefore"));
	        config.setAspectAfter(this.reader.getConfig().getProperty("aspectAfter"));
	        config.setAspectAfterThrow(this.reader.getConfig().getProperty("aspectAfterThrow"));
	        config.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspectAfterThrowingName"));
	        return new LFAdvisedSupport(config);
	 }
	 
	 
	 private LFAopProxy createProxy(LFAdvisedSupport config) {
	        Class targetClass = config.getTargetClass();
	        if(targetClass.getInterfaces().length > 0){
	            return new LFJdkDynamicAopProxy(config);
	        }
	        return new LFCglibAopProxy(config);
	 }
	 
	//返回所有BeanDefinition
	public String[] getBeanDefinitionNames(){
		
		return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
	}
	
	//返回所有BeanDefinition个数
	public int getBeanDefinitionCount(){
		
		return this.beanDefinitionMap.size();
	}
	
	//返回配置信息
	public Properties getConfig(){
		
		return this.reader.getConfig();
	}
}

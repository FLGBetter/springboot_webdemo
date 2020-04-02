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
 * IOC核心容器
 *
 * Created by Tom.
 */
public class LFFApplicationContext extends LFDefaultListableBeanFactory implements LFBeanFactory {

    private String [] configLoactions;
    private LFBeanDefinitionReader reader;

    //单例的IOC容器缓存
    private Map<String,Object> factoryBeanObjectCache = new ConcurrentHashMap<String, Object>();
    //通用的IOC容器
    private Map<String,LFBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<String, LFBeanWrapper>();

    public LFFApplicationContext(String... configLoactions){
        this.configLoactions = configLoactions;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void refresh(){
        //1、定位，定位配置文件
        reader = new LFBeanDefinitionReader(this.configLoactions);

        //2、加载配置文件，扫描相关的类，把它们封装成BeanDefinition
        List<LFBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        //3、注册，把配置信息放到容器里面(伪IOC容器)
        try {
			doRegisterBeanDefinition(beanDefinitions);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        //4、把不是延时加载的类，有提前初始化
        doAutowrited();
    }

    //只处理非延时加载的情况
    private void doAutowrited() {
        for (Map.Entry<String, LFBeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()) {
           String beanName = beanDefinitionEntry.getKey();
           if(!beanDefinitionEntry.getValue().isLazyInit()) {
               try {
                   getBean(beanName);
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
        }
    }

    private void doRegisterBeanDefinition(List<LFBeanDefinition> beanDefinitions) throws Exception {

        for (LFBeanDefinition beanDefinition: beanDefinitions) {
            if(super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("The “" + beanDefinition.getFactoryBeanName() + "” is exists!!");
            }
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
        }
        //到这里为止，容器初始化完毕
    }
    public Object getBean(Class<?> beanClass) throws Exception {
        return getBean(beanClass.getName());
    }

    //依赖注入，从这里开始，通过读取BeanDefinition中的信息
    //然后，通过反射机制创建一个实例并返回
    //Spring做法是，不会把最原始的对象放出去，会用一个BeanWrapper来进行一次包装
    //装饰器模式：
    //1、保留原来的OOP关系
    //2、我需要对它进行扩展，增强（为了以后AOP打基础）
    public Object getBean(String beanName) throws Exception {

        LFBeanDefinition gpBeanDefinition = this.beanDefinitionMap.get(beanName);
        Object instance = null;

        //这个逻辑还不严谨，自己可以去参考Spring源码
        //工厂模式 + 策略模式
        LFBeanPostProcessor postProcessor = new LFBeanPostProcessor();

        postProcessor.postProcessBeforeInitialization(instance,beanName);

        instance = instantiateBean(beanName,gpBeanDefinition);

        //3、把这个对象封装到BeanWrapper中
        LFBeanWrapper beanWrapper = new LFBeanWrapper(instance);

        //4、把BeanWrapper存到IOC容器里面
//        //1、初始化

//        //class A{ B b;}
//        //class B{ A a;}
//        //先有鸡还是先有蛋的问题，一个方法是搞不定的，要分两次

        //2、拿到BeanWraoper之后，把BeanWrapper保存到IOC容器中去
        this.factoryBeanInstanceCache.put(beanName,beanWrapper);

        postProcessor.postProcessAfterInitialization(instance,beanName);

//        //3、注入
        populateBean(beanName,new LFBeanDefinition(),beanWrapper);


        return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
    }

    private void populateBean(String beanName, LFBeanDefinition gpBeanDefinition, LFBeanWrapper gpBeanWrapper) {
        Object instance = gpBeanWrapper.getWrappedInstance();

//        gpBeanDefinition.getBeanClassName();

        Class<?> clazz = gpBeanWrapper.getWrappedClass();
        //判断只有加了注解的类，才执行依赖注入
        if(!(clazz.isAnnotationPresent(LFController.class) || clazz.isAnnotationPresent(LFService.class))){
            return;
        }

        //获得所有的fields
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if(!field.isAnnotationPresent(LFAutowired.class)){ continue;}

            LFAutowired autowired = field.getAnnotation(LFAutowired.class);

            String autowiredBeanName =  autowired.value().trim();
            if("".equals(autowiredBeanName)){
                autowiredBeanName = field.getType().getName();
            }

            //强制访问
            field.setAccessible(true);

            try {
                //为什么会为NULL，先留个坑
                if(this.factoryBeanInstanceCache.get(autowiredBeanName) == null){ continue; }
//                if(instance == null){
//                    continue;
//                }
                field.set(instance,this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }

    private Object instantiateBean(String beanName, LFBeanDefinition gpBeanDefinition) {
        //1、拿到要实例化的对象的类名
        String className = gpBeanDefinition.getBeanClassName();

        //2、反射实例化，得到一个对象
        Object instance = null;
        try {
//            gpBeanDefinition.getFactoryBeanName()
            //假设默认就是单例,细节暂且不考虑，先把主线拉通
            if(this.factoryBeanObjectCache.containsKey(className)){
                instance = this.factoryBeanObjectCache.get(className);
            }else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();

                LFAdvisedSupport config = instantionAopConfig(gpBeanDefinition);
                config.setTargetClass(clazz);
                config.setTarget(instance);

                //符合PointCut的规则的话，闯将代理对象
                if(config.pointCutMatch()) {
                    instance = createProxy(config).getProxy();
                }

                this.factoryBeanObjectCache.put(className,instance);
                this.factoryBeanObjectCache.put(gpBeanDefinition.getFactoryBeanName(),instance);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return instance;
    }

    private LFAopProxy createProxy(LFAdvisedSupport config) {

        Class targetClass = config.getTargetClass();
        if(targetClass.getInterfaces().length > 0){
            return new LFJdkDynamicAopProxy(config);
        }
        return new LFCglibAopProxy(config);
    }

    private LFAdvisedSupport instantionAopConfig(LFBeanDefinition gpBeanDefinition) {
        LFAopConfig config = new LFAopConfig();
        config.setPointCut(this.reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(this.reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(this.reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(this.reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(this.reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspectAfterThrowingName"));
        return new LFAdvisedSupport(config);
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new  String[this.beanDefinitionMap.size()]);
    }

    public int getBeanDefinitionCount(){
        return this.beanDefinitionMap.size();
    }

    public Properties getConfig(){
        return this.reader.getConfig();
    }
}

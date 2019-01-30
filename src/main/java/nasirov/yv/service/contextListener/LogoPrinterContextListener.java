package nasirov.yv.service.contextListener;

import nasirov.yv.service.annotation.PrintApplicationLogo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Created by Хикка on 25.01.2019.
 */
@Component
public class LogoPrinterContextListener implements ApplicationListener<ContextRefreshedEvent> {
	private static final Logger logger = LoggerFactory.getLogger(LogoPrinterContextListener.class);
	
	private ConfigurableListableBeanFactory factory;
	
	@Autowired
	public LogoPrinterContextListener(ConfigurableListableBeanFactory factory) {
		this.factory = factory;
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
		ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
		String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
		for (String bean : beanDefinitionNames) {
			BeanDefinition beanDefinition = factory.getBeanDefinition(bean);
			String beanClassName = beanDefinition.getBeanClassName();
			if (beanClassName != null) {
				try {
					Class<?> beanClass = Class.forName(beanClassName);
					if (beanClass.isAnnotationPresent(PrintApplicationLogo.class)) {
						Method[] methods = beanClass.getMethods();
						for (Method method : methods) {
							if (method.isAnnotationPresent(PrintApplicationLogo.class)) {
								Object currentBean = applicationContext.getBean(bean);
								Method currentMethod = currentBean.getClass().getMethod(method.getName(), method.getParameterTypes());
								currentMethod.invoke(currentBean);
							}
						}
					}
				} catch (Exception e) {
					logger.error("Exception while printing application logo", e);
				}
			}
		}
	}
}

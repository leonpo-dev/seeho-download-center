package com.seeho.downloadcenter.domain.utils;

import com.seeho.downloadcenter.base.exception.BusinessException;
import org.springframework.beans.BeansException;
import org.springframework.util.Assert;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Exposes the Spring {@link ApplicationContext} for non-managed components.
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    public static ApplicationContext getApplicationContext() {
        assertContextInjected();
        return applicationContext;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) {
        assertContextInjected();
        try {
            return (T) applicationContext.getBean(beanName);
        } catch (BeansException e) {
            throw new BusinessException("Failed to get bean with name: " + beanName + ", error: " + e.getMessage());
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        assertContextInjected();
        try {
            return applicationContext.getBean(clazz);
        } catch (BeansException e) {
            throw new BusinessException("Failed to get bean of type: " + clazz.getName() + ", error: " + e.getMessage());
        }
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        assertContextInjected();
        try {
            return applicationContext.getBean(beanName, clazz);
        } catch (BeansException e) {
            throw new BusinessException("Failed to get bean with name: " + beanName
                    + " and type: " + clazz.getName() + ", error: " + e.getMessage());
        }
    }

    private static void assertContextInjected() {
        Assert.notNull(applicationContext,
                "ApplicationContext has not been injected, please check if SpringContextHolder is managed by Spring");
    }
}

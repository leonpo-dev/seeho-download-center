package com.seeho.downloadcenter.domain.utils;

import com.seeho.downloadcenter.base.exception.BusinessException;
import org.springframework.beans.BeansException;
import org.springframework.util.Assert;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Spring容器工具类
 * 用于在非Spring管理的类中获取Spring容器中的Bean
 *
 * @author Leonpo
 * @since 2025-11-26
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext实例
     */
    public static ApplicationContext getApplicationContext() {
        assertContextInjected();
        return applicationContext;
    }

    /**
     * 根据Bean名称获取Bean
     *
     * @param beanName Bean名称
     * @param <T>      Bean类型
     * @return Bean实例
     * @throws BusinessException 当Bean不存在或容器未初始化时
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) {
        assertContextInjected();
        try {
            return (T) applicationContext.getBean(beanName);
        } catch (BeansException e) {
            throw new BusinessException("Failed to get bean with name: " + beanName + ", error: " + e.getMessage());
        }
    }

    /**
     * 根据Class类型获取Bean
     *
     * @param clazz Bean的Class类型
     * @param <T>   Bean类型
     * @return Bean实例
     * @throws BusinessException 当Bean不存在或容器未初始化时
     */
    public static <T> T getBean(Class<T> clazz) {
        assertContextInjected();
        try {
            return applicationContext.getBean(clazz);
        } catch (BeansException e) {
            throw new BusinessException("Failed to get bean of type: " + clazz.getName() + ", error: " + e.getMessage());
        }
    }

    /**
     * 根据Bean名称和Class类型获取Bean
     *
     * @param beanName Bean名称
     * @param clazz    Bean的Class类型
     * @param <T>      Bean类型
     * @return Bean实例
     * @throws BusinessException 当Bean不存在或容器未初始化时
     */
    public static <T> T getBean(String beanName, Class<T> clazz) {
        assertContextInjected();
        try {
            return applicationContext.getBean(beanName, clazz);
        } catch (BeansException e) {
            throw new BusinessException("Failed to get bean with name: " + beanName
                    + " and type: " + clazz.getName() + ", error: " + e.getMessage());
        }
    }

    /**
     * 断言ApplicationContext已注入
     *
     * @throws BusinessException 当ApplicationContext未初始化时
     */
    private static void assertContextInjected() {
        Assert.notNull(applicationContext,
                "ApplicationContext has not been injected, please check if SpringContextHolder is managed by Spring");
    }
}

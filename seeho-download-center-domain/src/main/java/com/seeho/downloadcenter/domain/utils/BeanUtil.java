package com.seeho.downloadcenter.domain.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight wrappers around Spring's {@link BeanUtils}.
 */
@Slf4j
public class BeanUtil {

    public static <T> T copy(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            log.error("Failed to copy bean from {} to {}", source.getClass().getName(), targetClass.getName(), e);
            throw new RuntimeException("Failed to copy bean", e);
        }
    }

    public static <T> List<T> copyList(List<?> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> targetList = new ArrayList<>(sourceList.size());
        for (Object source : sourceList) {
            targetList.add(copy(source, targetClass));
        }
        return targetList;
    }
}

package com.seeho.downloadcenter.domain.utils;

import java.util.Collection;

/**
 * 集合工具类
 *
 * @author Leonpo
 * @since 2025-12-02
 */
public class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }
}


package com.seeho.downloadcenter.domain.utils;

import java.util.Collection;

/**
 * Convenience helpers for collection null checks.
 */
public class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }
}

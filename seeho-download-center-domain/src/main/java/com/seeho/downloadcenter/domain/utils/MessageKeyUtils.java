package com.seeho.downloadcenter.domain.utils;

/**
 * Utility for building deterministic MQ message keys.
 */
public class MessageKeyUtils {

    public static String generate(String dlCode, Long taskId) {
        return String.format("%s-%d", dlCode, taskId);
    }
}

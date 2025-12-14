package com.seeho.downloadcenter.domain.utils;

/**
 * 消息Key生成工具类
 *
 * @author Leonpo
 * @since 2025-12-02
 */
public class MessageKeyUtils {

    /**
     * 生成消息Key
     *
     * @param dlCode 下载类型代码
     * @param taskId 任务ID
     * @return 消息Key
     */
    public static String generate(String dlCode, Long taskId) {
        return String.format("%s-%d", dlCode, taskId);
    }
}


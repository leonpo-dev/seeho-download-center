package com.seeho.downloadcenter.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 下载状态枚举
 *
 * @author Leonpo
 * @since 2025-11-25
 */
@Getter
@AllArgsConstructor
public enum DownloadStatusEnum {

    /**
     * 未执行
     */
    NOT_EXECUTED((byte) 0, "未执行"),

    /**
     * 正在执行
     */
    EXECUTING((byte) 1, "正在执行"),

    /**
     * 执行失败
     */
    FAILED((byte) 2, "执行失败"),

    /**
     * 执行成功
     */
    SUCCESS((byte) 3, "执行成功"),

    /**
     * 取消执行
     */
    CANCELLED((byte) 4, "取消执行"),

    /**
     * 消息发送失败
     */
    SEND_FAILED((byte) 5, "消息发送失败");

    /**
     * 状态码
     */
    private final Byte code;

    /**
     * 状态描述
     */
    private final String desc;

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 枚举值
     */
    public static DownloadStatusEnum getByCode(Byte code) {
        if (code == null) {
            return null;
        }
        for (DownloadStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}

package com.seeho.downloadcenter.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Lifecycle for a download task.
 */
@Getter
@AllArgsConstructor
public enum DownloadStatusEnum {

    NOT_EXECUTED((byte) 0, "Pending"),
    EXECUTING((byte) 1, "Running"),
    FAILED((byte) 2, "Failed"),
    SUCCESS((byte) 3, "Finished"),
    CANCELLED((byte) 4, "Cancelled"),
    SEND_FAILED((byte) 5, "Message dispatch failed");

    private final Byte code;
    private final String desc;

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

package com.seeho.downloadcenter.base.enums;

import com.seeho.downloadcenter.base.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Known download job implementations.
 */
@AllArgsConstructor
@Getter
public enum DownloadRefServiceEnum {

    DOWNLOAD_ZTO_BILLS("DOWNLOAD_ZTO_BILLS", "com.seeho.downloadcenter.domain.dotask.process.impl.ZTOBillsQueryImpl", 15L, "ZTO bill download service"),

    ;

    private String dlCode;
    private String contextBeanName;
    /** Delay for MQ messages (seconds); zero means immediate dispatch. */
    private Long timeout = 0L;
    private String desc;


    DownloadRefServiceEnum(String dlCode, String contextBeanName, String desc) {
        this.dlCode = dlCode;
        this.contextBeanName = contextBeanName;
        this.desc = desc;
    }

    public static DownloadRefServiceEnum matchDownloadType(String dlCode) {
        if (null == dlCode || dlCode.isEmpty()) {
            throw new BusinessException("Download type cannot be empty ");
        }
        String trimmedType = dlCode.trim();
        for (DownloadRefServiceEnum value : values()) {
            if (value.getDlCode().equalsIgnoreCase(trimmedType)) {
                return value;
            }
        }
        throw new BusinessException("Unsupported download type: " + dlCode);
    }
}

package com.seeho.downloadcenter.base.enums;

import com.seeho.downloadcenter.base.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 下载服务枚举
 *
 * @author Leonpo
 * @since 2025-11-25
 */
@AllArgsConstructor
@Getter
public enum DownloadRefServiceEnum {

    DOWNLOAD_ZTO_BILLS("DOWNLOAD_ZTO_BILLS", "com.seeho.downloadcenter.domain.dotask.process.impl.ZTOBillsQueryImpl", 15L, "中通账单下载服务"),

    ;

    private String dlCode;
    private String contextBeanName;
    /**
     * MQ延时消息，单位秒
     * 默认0表示不延时
     */
    private Long timeout = 0L;
    private String desc;


    DownloadRefServiceEnum(String dlCode, String contextBeanName, String desc) {
        this.dlCode = dlCode;
        this.contextBeanName = contextBeanName;
        this.desc = desc;
    }

    /**
     * 根据下载类型匹配枚举（带校验）
     *
     * @param dlCode 下载类型
     * @return 下载服务枚举
     * @throws BusinessException 当参数为空或枚举不存在时抛出
     */
    public static DownloadRefServiceEnum matchDownloadType(String dlCode) {
        // 参数校验
        if (null == dlCode || dlCode.isEmpty()) {
            throw new BusinessException("Download type cannot be empty ");
        }
        // 大小写不敏感匹配
        String trimmedType = dlCode.trim();
        for (DownloadRefServiceEnum value : values()) {
            if (value.getDlCode().equalsIgnoreCase(trimmedType)) {
                return value;
            }
        }

        // 未匹配到枚举抛出异常
        throw new BusinessException("Unsupported download type: " + dlCode);
    }
}

package com.seeho.downloadcenter.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Leonpo
 * @since 2025-11-25
 */
@Data
public class DownloadLogVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 下载名称
     */
    private String downloadName;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 下载类型（枚举值）
     */
    private String downloadType;

    /**
     * 下载状态：0=未执行，1=正在执行，2=执行失败, 3=执行成功, 4=取消执行
     */
    private Byte downloadStatus;

    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;


    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}

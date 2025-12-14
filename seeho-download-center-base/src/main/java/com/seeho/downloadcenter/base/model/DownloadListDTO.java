package com.seeho.downloadcenter.base.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DownloadListDTO {

    private Long id;
    private String downloadName;
    private Long userId;
    private String downloadType;

    /**
     * 下载状态：0=未执行，1=正在执行，2=执行失败, 3=执行成功, 4=取消执行
     */
    private Byte downloadStatus;
    private String fileUrl;
    private String fileName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

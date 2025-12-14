package com.seeho.downloadcenter.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DownloadLogVO {

    private Long id;
    private String downloadName;
    private Long userId;
    private String downloadType;
    private Byte downloadStatus;
    private String fileName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

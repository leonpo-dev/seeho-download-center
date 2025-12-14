package com.seeho.downloadcenter.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Persistence object for the download_log table.
 */
@Getter
@Setter
@TableName("download_log")
public class DownloadLogPO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String downloadName;
    private Long userId;
    private String downloadType;
    private Byte downloadStatus;
    private String failReason;
    private Integer retryCount;
    private String downloadCondition;
    private String titles;
    private String remark;
    private String fileUrl;

    private String fileName;

    private String messageKey;

    private Long createUserId;
    private LocalDateTime createTime;
    private Long updateUserId;
    private LocalDateTime updateTime;
}

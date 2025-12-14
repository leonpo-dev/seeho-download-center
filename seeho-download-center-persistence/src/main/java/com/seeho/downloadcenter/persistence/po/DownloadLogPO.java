package com.seeho.downloadcenter.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 文件下载日志记录表
 * </p>
 *
 * @author Leonpo
 * @since 2025-12-02
 */
@Getter
@Setter
@TableName("download_log")
public class DownloadLogPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * 失败原因
     */
    private String failReason;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 下载条件（JSON 或其他格式）
     */
    private String downloadCondition;

    /**
     * 导出表头（JSON 或字符串）
     */
    private String titles;

    /**
     * 备注
     */
    private String remark;

    /**
     * 文件下载地址
     */
    private String fileUrl;

    private String fileName;

    private String messageKey;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人ID
     */
    private Long updateUserId;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}

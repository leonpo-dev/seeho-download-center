package com.seeho.downloadcenter.base.model;



import com.seeho.downloadcenter.base.enums.DownloadRefServiceEnum;
import lombok.Data;

import java.util.List;

/**
 * @author Leonpo
 * @since 2025-11-25
 */
@Data
public class DownloadLogDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 下载名称
     */
    private String downloadName;

    /**
     * 下载类型（枚举值）
     */
    private DownloadRefServiceEnum downloadType;
    /**
     * 下载条件（JSON 或其他格式）
     */
    private String downloadCondition;

    /**
     * 导出表头配置（动态表头）
     * <p>
     * 前端传入的列配置列表，会在后续被序列化存入 DownloadLogPO.titles
     * </p>
     */
    private List<DownloadColumnDTO> titles;
}

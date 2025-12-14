package com.seeho.downloadcenter.request;

import com.seeho.downloadcenter.base.model.DownloadColumnDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * @author Leonpo
 * @since 2025-11-25
 */
@Data
public class DownloadLogRequest {

    /**
     * 下载名称
     */
    @NotBlank(message = "下载名称不能为空")
    @Size(max = 50, message = "下载名称长度不能超过50个字符")
    private String downloadName;

    /**
     * 下载类型（枚举值）
     *  @see com.seeho.downloadcenter.base.enums.DownloadRefServiceEnum
     */
    @NotBlank(message = "下载类型不能为空")
    private String downloadEnum;
    /**
     * 下载条件（JSON 或其他格式）
     */
    @NotBlank(message = "下载条件不能为空")
    private String downloadCondition;

    /**
     * 导出表头配置（动态表头）
     * <p>
     * 前端传入的列配置列表，会在后续被序列化存入 DownloadLogPO.titles
     * </p>
     */
    private List<DownloadColumnDTO> titles;
}

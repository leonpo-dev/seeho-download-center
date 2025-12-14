package com.seeho.downloadcenter.base.model;


import lombok.Data;

/**
 * 导出列配置对象
 * <p>
 * 用于前端传递动态表头配置：字段标识、显示名称、是否导出、格式化信息等
 * </p>
 *
 * 字段说明：
 * - field:   后端识别用的字段标识（如 billCode、recSiteName）
 * - header:  Excel 列头显示名称（如 "单号"、"收件网点"）
 * - enable:  是否导出该列
 * - dateFormat:  日期格式（可选），例如 "yyyy-MM-dd HH:mm:ss"
 * - numberFormat: 数字格式（可选），例如 "#,##0.00"
 *
 * @author Leonpo
 * @since 2025-11-27
 */
@Data
public class DownloadColumnDTO {

    /**
     * 字段标识（用于后端取值映射）
     */
    private String field;

    /**
     * 列头显示名称
     */
    private String header;

    /**
     * 是否导出该列
     */
    private Boolean enable = Boolean.TRUE;

    /**
     * 日期格式（可选），例如：yyyy-MM-dd HH:mm:ss
     */
    private String dateFormat;

    /**
     * 数字格式（可选），例如：#,##0.00
     */
    private String numberFormat;
}

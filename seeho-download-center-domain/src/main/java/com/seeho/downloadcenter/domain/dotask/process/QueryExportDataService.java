package com.seeho.downloadcenter.domain.dotask.process;

import com.fasterxml.jackson.core.type.TypeReference;
import com.alibaba.excel.util.StringUtils;
import com.seeho.downloadcenter.base.common.PageRequest;
import com.seeho.downloadcenter.domain.utils.JsonUtil;
import com.seeho.downloadcenter.domain.utils.LocalDateTimeUtils;
import com.seeho.downloadcenter.domain.utils.ParamSplitUtils;
import com.seeho.downloadcenter.base.model.DownloadColumnDTO;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface QueryExportDataService<C extends PageRequest, R> {

    /**
     * 转换下载条件
     *
     * @param downloadCondition
     * @return
     */
    C convertedDownloadCondition(String downloadCondition);

    /**
     * 查询导出数据总数
     *
     * @param condition
     * @return
     */
    Long queryTotalCount(C condition);

    /**
     * 查询导出数据
     *
     * @param condition
     * @return
     */
    List<R> queryExportData(C condition);

    /**
     * 初始化二分查询条件工具类
     *
     * @param condition
     * @return
     */
    ParamSplitUtils<C> initSplitUtils(C condition);

    /**
     * 获取导出数据的类型
     * <p>
     * 用于 Excel 表头生成，返回导出行 DTO 的 Class 对象
     * </p>
     *
     * @return 导出行数据类型
     */
    Class<R> getExportDataClass();

    /**
     * 提供字段映射器（强制实现）
     * <p>
     * 由具体业务实现，定义 field → DTO取值 的映射关系
     * </p>
     *
     * @return 字段映射器（field → 取值函数）
     */
    Map<String, Function<R, Object>> getFieldMapper();

    /**
     * 解析动态表头（default 方法）
     * <p>
     * 通用逻辑：
     * 1. 反序列化 titlesJson
     * 2. 过滤 enable=true 的列
     * 3. 构建 List<List<String>> 格式的表头
     * </p>
     *
     * @param titlesJson 前端传来的列配置 JSON
     * @return 动态表头；null 表示使用注解表头
     */
    default List<List<String>> parseDynamicHead(String titlesJson) {
        if (StringUtils.isBlank(titlesJson)) {
            return null;
        }

        try {
            List<DownloadColumnDTO> columns = JsonUtil.fromJson(
                    titlesJson,
                    new TypeReference<List<DownloadColumnDTO>>() {
                    }
            );

            if (columns == null || columns.isEmpty()) {
                return null;
            }

            // 过滤启用的列，构建表头
            return columns.stream()
                    .filter(col -> Boolean.TRUE.equals(col.getEnable()))
                    .map(col -> Collections.singletonList(col.getHeader()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            // 注意：接口中不能直接用 log，这里用 System.err 或让调用方处理
            System.err.println("[QueryExportDataService] Failed to parse dynamic head from titlesJson: " + e.getMessage());
            return null;
        }
    }

    /**
     * 生成行映射器（default 方法）
     * <p>
     * 根据 titlesJson 和业务提供的 fieldMapper，动态构建行数据
     * </p>
     *
     * @param titlesJson 前端传来的列配置 JSON
     * @return 行映射函数；null 表示使用注解表头
     */
    default Function<R, List<Object>> buildRowMapper(String titlesJson) {
        if (StringUtils.isBlank(titlesJson)) {
            return null;
        }

        try {
            List<DownloadColumnDTO> columns = JsonUtil.fromJson(
                    titlesJson,
                    new TypeReference<List<DownloadColumnDTO>>() {
                    }
            );

            if (columns == null || columns.isEmpty()) {
                return null;
            }

            // 过滤启用的列
            List<DownloadColumnDTO> enabledColumns = columns.stream()
                    .filter(col -> Boolean.TRUE.equals(col.getEnable()))
                    .collect(Collectors.toList());

            // 获取业务提供的字段映射器
            Map<String, Function<R, Object>> fieldMapper = getFieldMapper();

            // 构建行映射函数
            return (R dto) -> {
                List<Object> row = new ArrayList<>(enabledColumns.size());
                for (DownloadColumnDTO col : enabledColumns) {
                    Function<R, Object> getter = fieldMapper.get(col.getField());
                    if (getter != null) {
                        Object value = getter.apply(dto);
                        // 应用格式化（日期、数字）
                        row.add(formatValue(value, col));
                    } else {
                        row.add(null); // 未识别字段填 null
                    }
                }
                return row;
            };

        } catch (Exception e) {
            System.err.println("[QueryExportDataService] Failed to build row mapper from titlesJson: " + e.getMessage());
            return null;
        }
    }

    /**
     * 格式化值（default 方法，业务可重写）
     * <p>
     * 根据列配置的 dateFormat、numberFormat 进行格式化
     * </p>
     *
     * @param value 原始值
     * @param col   列配置
     * @return 格式化后的值
     */
    default Object formatValue(Object value, DownloadColumnDTO col) {
        if (value == null) {
            return null;
        }

        // 日期格式化
        if (StringUtils.isNotBlank(col.getDateFormat())) {
            if (value instanceof LocalDate) {
                return LocalDateTimeUtils.convertLDToString((LocalDate) value, col.getDateFormat());
            } else if (value instanceof LocalDateTime) {
                return LocalDateTimeUtils.convertLDTToString((LocalDateTime) value, col.getDateFormat());
            }
        }

        // 数字格式化
        if (StringUtils.isNotBlank(col.getNumberFormat())) {
            if (value instanceof Number) {
                DecimalFormat df = new DecimalFormat(col.getNumberFormat());
                return df.format(value);
            }
        }

        return value;
    }
}

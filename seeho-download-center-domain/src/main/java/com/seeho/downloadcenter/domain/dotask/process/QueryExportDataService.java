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

    /** Converts the persisted condition JSON into a query object. */
    C convertedDownloadCondition(String downloadCondition);

    /** Counts the total number of rows for the export. */
    Long queryTotalCount(C condition);

    /** Fetches a page of export data. */
    List<R> queryExportData(C condition);

    /** Initializes the helper that splits large parameter sets. */
    ParamSplitUtils<C> initSplitUtils(C condition);

    /** Provides the DTO class used by the Excel writer. */
    Class<R> getExportDataClass();

    /** Maps field identifiers to DTO getters for dynamic exports. */
    Map<String, Function<R, Object>> getFieldMapper();

    /** Parses client provided column definitions and builds an EasyExcel head. */
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

            return columns.stream()
                    .filter(col -> Boolean.TRUE.equals(col.getEnable()))
                    .map(col -> Collections.singletonList(col.getHeader()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("[QueryExportDataService] Failed to parse dynamic head from titlesJson: " + e.getMessage());
            return null;
        }
    }

    /** Builds a row mapper based on the provided column definition. */
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

            List<DownloadColumnDTO> enabledColumns = columns.stream()
                    .filter(col -> Boolean.TRUE.equals(col.getEnable()))
                    .collect(Collectors.toList());

            Map<String, Function<R, Object>> fieldMapper = getFieldMapper();

            return (R dto) -> {
                List<Object> row = new ArrayList<>(enabledColumns.size());
                for (DownloadColumnDTO col : enabledColumns) {
                    Function<R, Object> getter = fieldMapper.get(col.getField());
                    if (getter != null) {
                        Object value = getter.apply(dto);
                        row.add(formatValue(value, col));
                    } else {
                        row.add(null);
                    }
                }
                return row;
            };

        } catch (Exception e) {
            System.err.println("[QueryExportDataService] Failed to build row mapper from titlesJson: " + e.getMessage());
            return null;
        }
    }

    /** Applies optional date/number formatting rules defined by the column. */
    default Object formatValue(Object value, DownloadColumnDTO col) {
        if (value == null) {
            return null;
        }

        if (StringUtils.isNotBlank(col.getDateFormat())) {
            if (value instanceof LocalDate) {
                return LocalDateTimeUtils.convertLDToString((LocalDate) value, col.getDateFormat());
            } else if (value instanceof LocalDateTime) {
                return LocalDateTimeUtils.convertLDTToString((LocalDateTime) value, col.getDateFormat());
            }
        }

        if (StringUtils.isNotBlank(col.getNumberFormat())) {
            if (value instanceof Number) {
                DecimalFormat df = new DecimalFormat(col.getNumberFormat());
                return df.format(value);
            }
        }

        return value;
    }
}

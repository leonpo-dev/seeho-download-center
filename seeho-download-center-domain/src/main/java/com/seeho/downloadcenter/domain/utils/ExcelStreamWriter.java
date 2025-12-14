package com.seeho.downloadcenter.domain.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.seeho.downloadcenter.base.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Excel 流式写入工具类
 * <p>
 * 支持大数据量分批查询与流式写入，自动管理资源释放，
 * 超过百万行自动切换 Sheet，避免内存溢出。
 * </p>
 *
 * @author Leonpo
 * @since 2025-11-26
 */
@Slf4j
public class ExcelStreamWriter {

    /**
     * 单个 Sheet 最大行数（Excel 上限约 1,048,576，保守设置为 1,000,000）
     */
    private static final int MAX_ROWS_PER_SHEET = 1_000_000;

    /**
     * 分批写入 Excel（注解表头模式）
     * <p>
     * 根据条件列表逐批查询数据并写入 Excel，自动管理 Sheet 切换与资源释放
     * </p>
     *
     * @param filePathPrefix 文件路径前缀（如 /data/export/download-zto-bills-123）
     * @param headClass      Excel 表头类（需要标注 @ExcelProperty）
     * @param conditionList  查询条件列表（已按时间排序）
     * @param dataFetcher    数据查询函数（条件 → 数据列表）
     * @param <C>            查询条件类型
     * @param <R>            导出行数据类型
     * @return 最终生成的文件完整路径
     * @throws BusinessException 当写入失败或查询异常时
     */
    public static <C, R> String writeBatchData(String filePathPrefix,
                                                Class<R> headClass,
                                                List<C> conditionList,
                                                Function<C, List<R>> dataFetcher) {
        return writeBatchDataInternal(
                filePathPrefix,
                conditionList,
                dataFetcher,
                // 创建注解表头模式的 ExcelWriter
                filePath -> EasyExcel.write(filePath, headClass).build(),
                // 创建注解表头模式的 WriteSheet
                sheetIndex -> EasyExcel.writerSheet(sheetIndex, "Sheet" + (sheetIndex + 1)).build(),
                // 直接写入原始数据
                (batchData, rowMapper) -> (List<List<Object>>) (List<?>) batchData,
                null,
                "Annotation Mode"
        );
    }

    /**
     * 分批写入 Excel（动态表头模式）
     * <p>
     * 根据条件列表逐批查询数据并写入 Excel，支持动态表头和行映射
     * </p>
     *
     * @param filePathPrefix 文件路径前缀（如 /data/export/download-zto-bills-123）
     * @param head           动态表头（List<List<String>>）
     * @param conditionList  查询条件列表（已按时间排序）
     * @param dataFetcher    数据查询函数（条件 → 数据列表）
     * @param rowMapper      行映射函数（DTO → List<Object>）
     * @param <C>            查询条件类型
     * @param <R>            导出行数据类型
     * @return 最终生成的文件完整路径
     * @throws BusinessException 当写入失败或查询异常时
     */
    public static <C, R> String writeDynamicWithMapper(String filePathPrefix,
                                                        List<List<String>> head,
                                                        List<C> conditionList,
                                                        Function<C, List<R>> dataFetcher,
                                                        Function<R, List<Object>> rowMapper) {
        return writeBatchDataInternal(
                filePathPrefix,
                conditionList,
                dataFetcher,
                // 创建动态表头模式的 ExcelWriter
                filePath -> EasyExcel.write(filePath).build(),
                // 创建动态表头模式的 WriteSheet
                sheetIndex -> EasyExcel.writerSheet(sheetIndex, "Sheet" + (sheetIndex + 1)).head(head).build(),
                // 将 DTO 映射为行数据
                (batchData, mapper) -> {
                    List<List<Object>> rows = new ArrayList<>(batchData.size());
                    for (R dto : batchData) {
                        rows.add(mapper.apply(dto));
                    }
                    return rows;
                },
                rowMapper,
                "Dynamic Mode"
        );
    }

    /**
     * 分批写入 Excel 的内部实现（核心逻辑）
     *
     * @param filePathPrefix    文件路径前缀
     * @param conditionList     查询条件列表
     * @param dataFetcher       数据查询函数
     * @param writerFactory     ExcelWriter 创建工厂
     * @param sheetFactory      WriteSheet 创建工厂
     * @param dataConverter     数据转换器（将查询结果转换为可写入的行数据）
     * @param rowMapper         行映射函数（可选，用于动态表头模式）
     * @param mode              模式标识（用于日志）
     * @param <C>               查询条件类型
     * @param <R>               导出行数据类型
     * @return 最终生成的文件完整路径
     */
    private static <C, R> String writeBatchDataInternal(String filePathPrefix,
                                                         List<C> conditionList,
                                                         Function<C, List<R>> dataFetcher,
                                                         Function<String, ExcelWriter> writerFactory,
                                                         Function<Integer, WriteSheet> sheetFactory,
                                                         BiFunction<List<R>, Function<R, List<Object>>, List<List<Object>>> dataConverter,
                                                         Function<R, List<Object>> rowMapper,
                                                         String mode) {
        // 生成文件完整路径（拼接时间戳后缀）
        String filePath = filePathPrefix + "-" + System.currentTimeMillis() + ".xlsx";

        log.info("[ExcelStreamWriter] Start writing Excel ({}), filePath={}, conditionCount={}", mode, filePath, conditionList.size());

        ExcelWriter excelWriter = null;
        try {
            // 创建 ExcelWriter
            excelWriter = writerFactory.apply(filePath);

            // 当前 Sheet 序号与行计数
            int currentSheetIndex = 0;
            int currentSheetRows = 0;
            WriteSheet currentSheet = sheetFactory.apply(currentSheetIndex);

            // 逐条件批次查询并写入
            int batchIndex = 0;
            for (C condition : conditionList) {
                batchIndex++;
                log.debug("[ExcelStreamWriter] Processing batch {}/{}, condition={}", batchIndex, conditionList.size(), condition);

                // 查询当前批次数据
                List<R> batchData;
                try {
                    batchData = dataFetcher.apply(condition);
                } catch (Exception e) {
                    log.error("[ExcelStreamWriter] Failed to fetch data for condition: {}", condition, e);
                    throw new BusinessException("Failed to query export data: " + e.getMessage(), e);
                }

                // 若当前批次无数据，跳过
                if (batchData == null || batchData.isEmpty()) {
                    log.debug("[ExcelStreamWriter] Batch {} has no data, skip", batchIndex);
                    continue;
                }

                int batchSize = batchData.size();
                log.debug("[ExcelStreamWriter] Batch {} fetched {} rows", batchIndex, batchSize);

                // 检查是否需要切换 Sheet
                if (currentSheetRows + batchSize > MAX_ROWS_PER_SHEET) {
                    log.info("[ExcelStreamWriter] Current sheet rows={}, exceeds limit, switching to next sheet", currentSheetRows);
                    currentSheetIndex++;
                    currentSheetRows = 0;
                    currentSheet = sheetFactory.apply(currentSheetIndex);
                }

                // 转换并写入数据
                List<List<Object>> writeData = dataConverter.apply(batchData, rowMapper);
                try {
                    excelWriter.write(writeData, currentSheet);
                    currentSheetRows += batchSize;
                    log.debug("[ExcelStreamWriter] Batch {} written successfully, currentSheetRows={}", batchIndex, currentSheetRows);
                } catch (Exception e) {
                    log.error("[ExcelStreamWriter] Failed to write batch {}", batchIndex, e);
                    throw new BusinessException("Failed to write Excel: " + e.getMessage(), e);
                }

                // 清空批次数据，帮助 GC
                batchData.clear();
                writeData.clear();
            }

            log.info("[ExcelStreamWriter] Excel writing completed ({}), filePath={}, totalSheets={}, lastSheetRows={}",
                    mode, filePath, currentSheetIndex + 1, currentSheetRows);

            return filePath;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[ExcelStreamWriter] Unexpected error during Excel writing ({})", mode, e);
            throw new BusinessException("Excel writing failed: " + e.getMessage(), e);
        } finally {
            if (excelWriter != null) {
                try {
                    excelWriter.finish();
                    log.debug("[ExcelStreamWriter] ExcelWriter closed successfully");
                } catch (Exception e) {
                    log.error("[ExcelStreamWriter] Failed to close ExcelWriter", e);
                }
            }
        }
    }
}

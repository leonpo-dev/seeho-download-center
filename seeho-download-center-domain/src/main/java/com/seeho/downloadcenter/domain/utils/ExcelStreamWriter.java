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
 * Streams large exports to Excel by batching queries and switching sheets automatically.
 */
@Slf4j
public class ExcelStreamWriter {

    private static final int MAX_ROWS_PER_SHEET = 1_000_000;

    public static <C, R> String writeBatchData(String filePathPrefix,
                                                Class<R> headClass,
                                                List<C> conditionList,
                                                Function<C, List<R>> dataFetcher) {
        return writeBatchDataInternal(
                filePathPrefix,
                conditionList,
                dataFetcher,
                filePath -> EasyExcel.write(filePath, headClass).build(),
                sheetIndex -> EasyExcel.writerSheet(sheetIndex, "Sheet" + (sheetIndex + 1)).build(),
                (batchData, rowMapper) -> (List<List<Object>>) (List<?>) batchData,
                null,
                "Annotation Mode"
        );
    }

    public static <C, R> String writeDynamicWithMapper(String filePathPrefix,
                                                        List<List<String>> head,
                                                        List<C> conditionList,
                                                        Function<C, List<R>> dataFetcher,
                                                        Function<R, List<Object>> rowMapper) {
        return writeBatchDataInternal(
                filePathPrefix,
                conditionList,
                dataFetcher,
                filePath -> EasyExcel.write(filePath).build(),
                sheetIndex -> EasyExcel.writerSheet(sheetIndex, "Sheet" + (sheetIndex + 1)).head(head).build(),
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

    private static <C, R> String writeBatchDataInternal(String filePathPrefix,
                                                         List<C> conditionList,
                                                         Function<C, List<R>> dataFetcher,
                                                         Function<String, ExcelWriter> writerFactory,
                                                         Function<Integer, WriteSheet> sheetFactory,
                                                         BiFunction<List<R>, Function<R, List<Object>>, List<List<Object>>> dataConverter,
                                                         Function<R, List<Object>> rowMapper,
                                                         String mode) {
        String filePath = filePathPrefix + "-" + System.currentTimeMillis() + ".xlsx";

        log.info("[ExcelStreamWriter] Start writing Excel ({}), filePath={}, conditionCount={}", mode, filePath, conditionList.size());

        ExcelWriter excelWriter = null;
        try {
            excelWriter = writerFactory.apply(filePath);

            int currentSheetIndex = 0;
            int currentSheetRows = 0;
            WriteSheet currentSheet = sheetFactory.apply(currentSheetIndex);

            int batchIndex = 0;
            for (C condition : conditionList) {
                batchIndex++;
                log.debug("[ExcelStreamWriter] Processing batch {}/{}, condition={}", batchIndex, conditionList.size(), condition);

                List<R> batchData;
                try {
                    batchData = dataFetcher.apply(condition);
                } catch (Exception e) {
                    log.error("[ExcelStreamWriter] Failed to fetch data for condition: {}", condition, e);
                    throw new BusinessException("Failed to query export data: " + e.getMessage(), e);
                }

                if (batchData == null || batchData.isEmpty()) {
                    log.debug("[ExcelStreamWriter] Batch {} has no data, skip", batchIndex);
                    continue;
                }

                int batchSize = batchData.size();
                log.debug("[ExcelStreamWriter] Batch {} fetched {} rows", batchIndex, batchSize);

                if (currentSheetRows + batchSize > MAX_ROWS_PER_SHEET) {
                    log.info("[ExcelStreamWriter] Current sheet rows={}, exceeds limit, switching to next sheet", currentSheetRows);
                    currentSheetIndex++;
                    currentSheetRows = 0;
                    currentSheet = sheetFactory.apply(currentSheetIndex);
                }

                List<List<Object>> writeData = dataConverter.apply(batchData, rowMapper);
                try {
                    excelWriter.write(writeData, currentSheet);
                    currentSheetRows += batchSize;
                    log.debug("[ExcelStreamWriter] Batch {} written successfully, currentSheetRows={}", batchIndex, currentSheetRows);
                } catch (Exception e) {
                    log.error("[ExcelStreamWriter] Failed to write batch {}", batchIndex, e);
                    throw new BusinessException("Failed to write Excel: " + e.getMessage(), e);
                }

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

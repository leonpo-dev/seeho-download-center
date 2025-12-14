package com.seeho.downloadcenter.domain.dotask.process;

import com.seeho.downloadcenter.base.common.PageRequest;
import org.springframework.util.Assert;
import com.seeho.downloadcenter.base.enums.DownloadRefServiceEnum;
import com.seeho.downloadcenter.domain.utils.ExcelStreamWriter;
import com.seeho.downloadcenter.domain.utils.ExportPathBuilder;
import com.seeho.downloadcenter.domain.utils.ParamSplitUtils;
import com.seeho.downloadcenter.persistence.po.DownloadLogPO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Coordinates parameter splitting, data querying, and Excel streaming.
 */
@Slf4j
@Service
public class ManagerDataProcess {
    /** Maximum rows fetched per query before we split parameters. */
    public static final Long SINGLE_MAX_COUNT = 10000L;

    @Resource
    private ExportPathBuilder exportPathBuilder;

    public <C extends PageRequest, R> String queryDataAndWriteExcel(QueryExportDataService<C, R> service,
                                                                         DownloadLogPO downloadLogPO) {
        log.info("[ManagerDataProcess] Start export, taskId={}, downloadName={}",
                downloadLogPO.getId(), downloadLogPO.getDownloadName());

        C condition = service.convertedDownloadCondition(downloadLogPO.getDownloadCondition());

        Long totalCount = service.queryTotalCount(condition);
        log.info("[ManagerDataProcess] Total count={}", totalCount);

        List<C> conditionList = new LinkedList<>();

        if (totalCount <= SINGLE_MAX_COUNT) {
            condition.setPageSize(SINGLE_MAX_COUNT.intValue());
            log.info("[ManagerDataProcess] Single query mode, totalCount <= {}", SINGLE_MAX_COUNT);
            conditionList.add(condition);
        } else {
            log.info("[ManagerDataProcess] Split mode, totalCount > {}", SINGLE_MAX_COUNT);
            ParamSplitUtils<C> splitUtils = service.initSplitUtils(condition);
            Assert.notNull(splitUtils, "ParamSplitUtils cannot be null");
            conditionList = splitUtils.split(condition);
            log.info("[ManagerDataProcess] Split completed, segmentCount={}", conditionList.size());
        }

        DownloadRefServiceEnum downloadEnum = DownloadRefServiceEnum.matchDownloadType(downloadLogPO.getDownloadType());
        String filePathPrefix = exportPathBuilder.buildFilePathPrefix(downloadEnum, downloadLogPO.getDownloadName());

        String titlesJson = downloadLogPO.getTitles();
        List<List<String>> head = service.parseDynamicHead(titlesJson);
        Function<R, List<Object>> rowMapper = service.buildRowMapper(titlesJson);

        String filePath;
        if (head != null && rowMapper != null) {
            log.info("[ManagerDataProcess] Using dynamic head mode");
            filePath = ExcelStreamWriter.writeDynamicWithMapper(
                    filePathPrefix,
                    head,
                    conditionList,
                    service::queryExportData,
                    rowMapper
            );
        } else {
            log.info("[ManagerDataProcess] Using static annotation head mode");
            filePath = ExcelStreamWriter.writeBatchData(
                    filePathPrefix,
                    service.getExportDataClass(),
                    conditionList,
                    service::queryExportData
            );
        }

        log.info("[ManagerDataProcess] Export completed, filePath={}", filePath);
        return filePath;
    }

}

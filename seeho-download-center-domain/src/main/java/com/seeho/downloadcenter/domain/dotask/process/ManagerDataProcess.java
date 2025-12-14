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
 * 数据导出管理器
 * <p>
 * 负责协调查询条件分片、数据查询与 Excel 流式写入
 * </p>
 *
 * @author Leonpo
 * @since 2025-11-26
 */
@Slf4j
@Service
public class ManagerDataProcess {
    /**
     * 单次查询最大数据量
     * <p>
     * 当总数据量超过此值时，触发条件分片逻辑
     * </p>
     */
    public static final Long SINGLE_MAX_COUNT = 10000L;

    @Resource
    private ExportPathBuilder exportPathBuilder;

    /**
     * 查询数据并写入 Excel（统一入口）
     * <p>
     * 自动判断是否使用动态表头，从 DownloadLogPO 中获取导出配置
     * </p>
     *
     * @param service       导出数据查询服务
     * @param downloadLogPO 下载任务记录（包含 downloadCondition、downloadName、titles）
     * @param <C>           查询条件类型
     * @param <R>           导出行数据类型
     * @return 生成的 Excel 文件完整路径
     */
    public <C extends PageRequest, R> String queryDataAndWriteExcel(QueryExportDataService<C, R> service,
                                                                         DownloadLogPO downloadLogPO) {
        log.info("[ManagerDataProcess] Start export, taskId={}, downloadName={}",
                downloadLogPO.getId(), downloadLogPO.getDownloadName());

        // 1. 转换查询条件
        C condition = service.convertedDownloadCondition(downloadLogPO.getDownloadCondition());

        // 2. 统计数据总数
        Long totalCount = service.queryTotalCount(condition);
        log.info("[ManagerDataProcess] Total count={}", totalCount);

        // 3. 收集查询条件列表
        List<C> conditionList = new LinkedList<>();

        if (totalCount <= SINGLE_MAX_COUNT) {
            condition.setPageSize(SINGLE_MAX_COUNT.intValue());
            // 单次查询即可
            log.info("[ManagerDataProcess] Single query mode, totalCount <= {}", SINGLE_MAX_COUNT);
            conditionList.add(condition);
        } else {
            // 需要分片查询
            log.info("[ManagerDataProcess] Split mode, totalCount > {}", SINGLE_MAX_COUNT);
            ParamSplitUtils<C> splitUtils = service.initSplitUtils(condition);
            Assert.notNull(splitUtils, "ParamSplitUtils cannot be null");
            conditionList = splitUtils.split(condition);
            log.info("[ManagerDataProcess] Split completed, segmentCount={}", conditionList.size());
        }

        // 4. 获取业务枚举并构建完整路径前缀
        DownloadRefServiceEnum downloadEnum = DownloadRefServiceEnum.matchDownloadType(downloadLogPO.getDownloadType());
        String filePathPrefix = exportPathBuilder.buildFilePathPrefix(downloadEnum, downloadLogPO.getDownloadName());

        // 5. 尝试解析动态表头
        String titlesJson = downloadLogPO.getTitles();
        List<List<String>> head = service.parseDynamicHead(titlesJson);
        Function<R, List<Object>> rowMapper = service.buildRowMapper(titlesJson);

        String filePath;
        if (head != null && rowMapper != null) {
            // 动态表头模式
            log.info("[ManagerDataProcess] Using dynamic head mode");
            filePath = ExcelStreamWriter.writeDynamicWithMapper(
                    filePathPrefix,
                    head,
                    conditionList,
                    service::queryExportData,
                    rowMapper
            );
        } else {
            // 静态注解表头模式
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

package com.seeho.downloadcenter.domain.job;

import com.seeho.downloadcenter.base.enums.DownloadStatusEnum;
import com.seeho.downloadcenter.domain.downloadlog.DownloadLogMangerService;
import com.seeho.downloadcenter.base.model.JobQueryDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Leonpo
 * @since 2025-12-11
 */
@Slf4j
@Component
@AllArgsConstructor
public class TaskJob {

    private final DownloadLogMangerService downloadLogMangerService;

    /**
     * 调用失败任务
     * TODO: XXL-Job removed - using Spring @Scheduled instead
     * 默认每5分钟执行一次，可通过配置修改
     */
    @Scheduled(cron = "${download.task.retry.cron:0 */5 * * * ?}")
    public void callFailTaskJob() {
        // 获取任务参数
        JobQueryDTO queryDTO = new JobQueryDTO();
        // 默认查询近1小时失败任务
        queryDTO.setStartTime(LocalDateTime.now().minusHours(1));
        queryDTO.setEndTime(LocalDateTime.now());
        queryDTO.setStatusList(List.of(DownloadStatusEnum.FAILED));

        Optional.ofNullable(downloadLogMangerService.queryToTaskList(queryDTO))
                .orElse(Collections.emptyList())
                .forEach(downloadLogMangerService::tryDoTaskAgain);
        log.info("【sendBillMsgJob】任务执行结束");
    }
}

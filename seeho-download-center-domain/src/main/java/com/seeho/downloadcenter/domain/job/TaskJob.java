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
 * Periodic job that retries failed download tasks.
 */
@Slf4j
@Component
@AllArgsConstructor
public class TaskJob {

    private final DownloadLogMangerService downloadLogMangerService;

    @Scheduled(cron = "${download.task.retry.cron:0 */5 * * * ?}")
    public void callFailTaskJob() {
        JobQueryDTO queryDTO = new JobQueryDTO();
        queryDTO.setStartTime(LocalDateTime.now().minusHours(1));
        queryDTO.setEndTime(LocalDateTime.now());
        queryDTO.setStatusList(List.of(DownloadStatusEnum.FAILED));

        Optional.ofNullable(downloadLogMangerService.queryToTaskList(queryDTO))
                .orElse(Collections.emptyList())
                .forEach(downloadLogMangerService::tryDoTaskAgain);
        log.info("[TaskJob] Retry job completed");
    }
}

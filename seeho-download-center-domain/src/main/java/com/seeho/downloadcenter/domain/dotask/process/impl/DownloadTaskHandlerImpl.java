package com.seeho.downloadcenter.domain.dotask.process.impl;

import com.seeho.downloadcenter.base.exception.BusinessException;
import com.seeho.downloadcenter.base.enums.DownloadRefServiceEnum;
import com.seeho.downloadcenter.base.enums.DownloadStatusEnum;
import com.seeho.downloadcenter.domain.dotask.process.DownloadTaskHandler;
import com.seeho.downloadcenter.domain.dotask.process.ManagerDataProcess;
import com.seeho.downloadcenter.domain.dotask.process.QueryExportDataService;
import com.seeho.downloadcenter.domain.dotask.process.TaskFailureCallback;
import com.seeho.downloadcenter.domain.downloadlog.DownloadLogMangerService;
import com.seeho.downloadcenter.domain.utils.SpringContextHolder;
import com.seeho.downloadcenter.persistence.po.DownloadLogPO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Default implementation that executes download/export tasks independently of the queue layer.
 */
@Slf4j
@Component
public class DownloadTaskHandlerImpl implements DownloadTaskHandler {

    @Resource
    private ManagerDataProcess managerDataProcess;

    @Resource
    private DownloadLogMangerService downloadLogMangerService;

    @Autowired(required = false)
    private TaskFailureCallback failureCallback;

    @Override
    public void handle(DownloadLogPO task) {
        Long taskId = task.getId();
        log.info("[DownloadTaskHandler] Start processing task, taskId={}, messageKey={}", taskId, task.getMessageKey());

        try {
            Boolean updated = downloadLogMangerService.updateDownloadLogStatus(
                    taskId,
                    DownloadStatusEnum.EXECUTING,
                    DownloadStatusEnum.NOT_EXECUTED
            );

            if (!updated) {
                log.warn("[DownloadTaskHandler] Task already processed or status changed, skip. taskId={}", taskId);
                return;
            }

            DownloadRefServiceEnum refServiceEnum = DownloadRefServiceEnum.matchDownloadType(task.getDownloadType());
            QueryExportDataService service = (QueryExportDataService) SpringContextHolder.getBean(Class.forName(refServiceEnum.getContextBeanName()));

            log.info("[DownloadTaskHandler] Start export, taskId={}", taskId);
            String filePath = managerDataProcess.queryDataAndWriteExcel(service, task);
            log.info("[DownloadTaskHandler] Export completed, taskId={}, filePath={}", taskId, filePath);

            task.setDownloadStatus(DownloadStatusEnum.SUCCESS.getCode());
            task.setFileUrl(filePath);
            Path path = Paths.get(filePath);
            String fileName = path.getFileName().toString();
            task.setFileName(fileName);
            downloadLogMangerService.updateDLPOById(task);

            log.info("[DownloadTaskHandler] Task processed successfully, taskId={}", taskId);

        } catch (BusinessException e) {
            log.error("[DownloadTaskHandler] Business error, taskId={}", taskId, e);

            updateTaskStatusToFailed(taskId, "Business error: " + e.getMessage());

            notifyFailure(task, e);

            throw e;

        } catch (Exception e) {
            log.error("[DownloadTaskHandler] System error, taskId={}", taskId, e);

            updateTaskStatusToFailed(taskId, "System error: " + e.getMessage());

            notifyFailure(task, e);

            throw new RuntimeException("Export failed, will retry", e);
        }
    }

    private void updateTaskStatusToFailed(Long taskId, String errorMessage) {
        try {
            DownloadLogPO logPO = new DownloadLogPO();
            logPO.setId(taskId);
            logPO.setDownloadStatus(DownloadStatusEnum.FAILED.getCode());
            logPO.setFailReason(errorMessage);
            downloadLogMangerService.updateDLPOById(logPO);
            log.info("[DownloadTaskHandler] Task status updated to FAILED, taskId={}", taskId);
        } catch (Exception ex) {
            log.error("[DownloadTaskHandler] Failed to update task status, taskId={}", taskId, ex);
            throw new RuntimeException("Failed to update task status to FAILED", ex);
        }
    }

    private void notifyFailure(DownloadLogPO task, Exception e) {
        if (failureCallback != null) {
            try {
                failureCallback.onTaskFailed(task, e);
            } catch (Exception callbackEx) {
                log.error("[DownloadTaskHandler] Failure callback error, taskId={}", task.getId(), callbackEx);
            }
        }
    }
}

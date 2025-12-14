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
 * 下载任务处理器实现
 * <p>
 * 负责执行下载导出任务的核心业务逻辑，与消息队列解耦
 * </p>
 *
 * @author Leonpo
 * @since 2025-11-26
 */
@Slf4j
@Component
public class DownloadTaskHandlerImpl implements DownloadTaskHandler {

    @Resource
    private ManagerDataProcess managerDataProcess;

    @Resource
    private DownloadLogMangerService downloadLogMangerService;

    /**
     * 可选注入失败回调（默认注入 NoOpTaskFailureCallback）
     */
    @Autowired(required = false)
    private TaskFailureCallback failureCallback;

    @Override
    public void handle(DownloadLogPO task) {
        Long taskId = task.getId();
        log.info("[DownloadTaskHandler] Start processing task, taskId={}, messageKey={}", taskId, task.getMessageKey());

        try {
            // 步骤1：乐观锁更新状态为"执行中"
            Boolean updated = downloadLogMangerService.updateDownloadLogStatus(
                    taskId,
                    DownloadStatusEnum.EXECUTING,
                    DownloadStatusEnum.NOT_EXECUTED
            );

            if (!updated) {
                // 状态不匹配，任务已被处理或状态已变更
                log.warn("[DownloadTaskHandler] Task already processed or status changed, skip. taskId={}", taskId);
                return; // 正常返回，避免重复处理
            }

            // 步骤2：获取服务实例
            DownloadRefServiceEnum refServiceEnum = DownloadRefServiceEnum.matchDownloadType(task.getDownloadType());
            QueryExportDataService service = (QueryExportDataService) SpringContextHolder.getBean(Class.forName(refServiceEnum.getContextBeanName()));

            // 步骤3：执行导出逻辑
            log.info("[DownloadTaskHandler] Start export, taskId={}", taskId);
            String filePath = managerDataProcess.queryDataAndWriteExcel(service, task);
            log.info("[DownloadTaskHandler] Export completed, taskId={}, filePath={}", taskId, filePath);

            // 步骤4：更新状态为"成功"
            task.setDownloadStatus(DownloadStatusEnum.SUCCESS.getCode());
            task.setFileUrl(filePath);
            // /Users/guanyf/data/export/2025-11/zto-bills/第一次下载ZTO账单2-1764237305805.xlsx
            Path path = Paths.get(filePath);
            String fileName = path.getFileName().toString();
            task.setFileName(fileName);
            downloadLogMangerService.updateDLPOById(task);

            log.info("[DownloadTaskHandler] Task processed successfully, taskId={}", taskId);

        } catch (BusinessException e) {
            // 业务异常：不应重试（如参数错误、枚举不匹配等）
            log.error("[DownloadTaskHandler] Business error, taskId={}", taskId, e);

            // 更新状态为"失败"
            updateTaskStatusToFailed(taskId, "Business error: " + e.getMessage());

            // 触发失败回调（预留 job 兜底入口）
            notifyFailure(task, e);

            // 向上抛出业务异常（MQ 消费者会捕获并 ACK，InMemory 消费者会记录日志并继续）
            throw e;

        } catch (Exception e) {
            // 系统异常：应该重试（如网络超时、数据库临时不可用等）
            log.error("[DownloadTaskHandler] System error, taskId={}", taskId, e);

            // 更新状态为"失败"
            updateTaskStatusToFailed(taskId, "System error: " + e.getMessage());

            // 触发失败回调（预留 job 兜底入口）
            notifyFailure(task, e);

            // 向上抛出系统异常（MQ 消费者会触发重试，InMemory 消费者会记录日志并继续）
            throw new RuntimeException("Export failed, will retry", e);
        }
    }

    /**
     * 更新任务状态为失败
     */
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
            // 状态更新失败也向上抛异常，让 job 兜底
            throw new RuntimeException("Failed to update task status to FAILED", ex);
        }
    }

    /**
     * 通知失败回调
     */
    private void notifyFailure(DownloadLogPO task, Exception e) {
        if (failureCallback != null) {
            try {
                failureCallback.onTaskFailed(task, e);
            } catch (Exception callbackEx) {
                log.error("[DownloadTaskHandler] Failure callback error, taskId={}", task.getId(), callbackEx);
                // 回调异常不影响主流程
            }
        }
    }
}

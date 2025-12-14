package com.seeho.downloadcenter.domain.dotask.process.impl;

import com.seeho.downloadcenter.domain.dotask.process.TaskFailureCallback;
import com.seeho.downloadcenter.persistence.po.DownloadLogPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 空实现的任务失败回调
 * <p>
 * 仅记录日志，不做实际处理，用于开发环境或未配置 job 的场景
 * </p>
 *
 * @author Leonpo
 * @since 2025-11-26
 */
@Slf4j
@Component
public class NoOpTaskFailureCallback implements TaskFailureCallback {

    @Override
    public void onTaskFailed(DownloadLogPO task, Exception e) {
        log.warn("[NoOpTaskFailureCallback] Task failed, waiting for job to handle. taskId={}, error={}",
                task.getId(), e.getMessage());
        // 预留 job 兜底入口，当前仅记录日志
    }
}

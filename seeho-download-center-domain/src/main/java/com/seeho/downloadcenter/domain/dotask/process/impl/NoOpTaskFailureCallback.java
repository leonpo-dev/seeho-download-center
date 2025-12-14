package com.seeho.downloadcenter.domain.dotask.process.impl;

import com.seeho.downloadcenter.domain.dotask.process.TaskFailureCallback;
import com.seeho.downloadcenter.persistence.po.DownloadLogPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Default failure callback used when no other handler is configured.
 * It simply records the error for observability.
 */
@Slf4j
@Component
public class NoOpTaskFailureCallback implements TaskFailureCallback {

    @Override
    public void onTaskFailed(DownloadLogPO task, Exception e) {
        log.warn("[NoOpTaskFailureCallback] Task failed, waiting for job to handle. taskId={}, error={}",
                task.getId(), e.getMessage());
    }
}

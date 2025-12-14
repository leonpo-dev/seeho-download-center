package com.seeho.downloadcenter.domain.dotask.process;

import com.seeho.downloadcenter.base.exception.BusinessException;
import com.seeho.downloadcenter.persistence.po.DownloadLogPO;

/**
 * Coordinates the lifecycle of a download/export task independent of the queue implementation.
 */
public interface DownloadTaskHandler {

    /** Executes the full processing pipeline for a single task. */
    void handle(DownloadLogPO task);
}

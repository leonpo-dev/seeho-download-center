package com.seeho.downloadcenter.domain.dotask.process;

import com.seeho.downloadcenter.persistence.po.DownloadLogPO;

/**
 * Callback hook used when download tasks fail.
 * Implementations can record metrics, trigger jobs, or notify other systems.
 */
public interface TaskFailureCallback {

    /** Invoked when a task cannot be completed. */
    void onTaskFailed(DownloadLogPO task, Exception e);
}

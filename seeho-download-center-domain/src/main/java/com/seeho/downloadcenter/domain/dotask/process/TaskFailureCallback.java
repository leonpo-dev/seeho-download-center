package com.seeho.downloadcenter.domain.dotask.process;

import com.seeho.downloadcenter.persistence.po.DownloadLogPO;

/**
 * 任务失败回调接口
 * <p>
 * 用于处理任务执行失败后的通知与兜底逻辑，与业务处理解耦
 * </p>
 *
 * @author Leonpo
 * @since 2025-11-26
 */
public interface TaskFailureCallback {

    /**
     * 任务处理失败时的回调
     * <p>
     * 用于异步 job 兜底、告警通知、统计等场景
     * </p>
     *
     * @param task 失败的任务对象
     * @param e    失败原因异常
     */
    void onTaskFailed(DownloadLogPO task, Exception e);
}

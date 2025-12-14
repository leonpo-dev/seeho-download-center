package com.seeho.downloadcenter.domain.dotask.process;

import com.seeho.downloadcenter.base.exception.BusinessException;
import com.seeho.downloadcenter.persistence.po.DownloadLogPO;

/**
 * 下载任务处理器接口
 * <p>
 * 负责处理下载导出任务的核心业务逻辑，与消息队列解耦
 * </p>
 *
 * @author Leonpo
 * @since 2025-11-26
 */
public interface DownloadTaskHandler {

    /**
     * 处理下载任务
     * <p>
     * 执行完整的任务处理流程：
     * 1. 乐观锁更新状态为"执行中"
     * 2. 获取服务实例并执行导出
     * 3. 更新状态为"成功"或"失败"
     * </p>
     *
     * @param task 下载任务对象
     * @throws BusinessException 业务异常（不应重试）
     * @throws RuntimeException                            系统异常（可重试）
     */
    void handle(DownloadLogPO task);
}

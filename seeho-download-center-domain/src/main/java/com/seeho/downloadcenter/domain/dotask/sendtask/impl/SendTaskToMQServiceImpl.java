package com.seeho.downloadcenter.domain.dotask.sendtask.impl;

import com.seeho.downloadcenter.domain.dotask.sendtask.SendTaskToMQService;
import com.seeho.downloadcenter.persistence.po.DownloadLogPO;
// TODO: RocketMQ integration removed - need to implement MQ producer if needed
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 基于 RocketMQ 的任务发送服务（测试/生产环境）
 * <p>
 * TODO: RocketMQ integration removed - this service is currently disabled.
 * Previously: 负责将任务发送到 RocketMQ 消息队列
 * </p>
 *
 * @author Leonpo
 * @since 2025-11-26
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "download.task.queue.mode", havingValue = "mq", matchIfMissing = false)
public class SendTaskToMQServiceImpl implements SendTaskToMQService {

    @Override
    public void sendTaskToMQ(DownloadLogPO downloadLogPO) {
        // TODO: RocketMQ integration removed - need to implement MQ producer if needed
        log.warn("[SendTaskToMQService] RocketMQ integration removed, task will not be sent to MQ. taskId={}",
                downloadLogPO != null ? downloadLogPO.getId() : "null");
        throw new UnsupportedOperationException("RocketMQ integration is not available. Please use memory queue mode.");
    }
}

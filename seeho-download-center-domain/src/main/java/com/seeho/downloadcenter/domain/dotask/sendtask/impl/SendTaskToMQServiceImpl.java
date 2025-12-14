package com.seeho.downloadcenter.domain.dotask.sendtask.impl;

import com.seeho.downloadcenter.domain.dotask.sendtask.SendTaskToMQService;
import com.seeho.downloadcenter.persistence.po.DownloadLogPO;
// TODO: RocketMQ integration removed - need to implement MQ producer if needed
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Placeholder for the RocketMQ implementation.
 * Currently disabled until the messaging integration is restored.
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

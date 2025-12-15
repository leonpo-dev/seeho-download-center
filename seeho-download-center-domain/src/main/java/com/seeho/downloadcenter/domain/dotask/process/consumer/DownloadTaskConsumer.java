package com.seeho.downloadcenter.domain.dotask.process.consumer;

import com.seeho.downloadcenter.domain.dotask.process.DownloadTaskHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Placeholder MQ consumer that delegates tasks to {@link DownloadTaskHandler}.
 * RocketMQ integration is currently disabled, the bean stays for future wiring.
 */
@Slf4j
@Component
// @ConditionalOnProperty(name = "download.task.queue.mode", havingValue = "mq", matchIfMissing = true)
public class DownloadTaskConsumer {

    @Resource
    private DownloadTaskHandler downloadTaskHandler;

    // TODO: RocketMQ integration removed - need to implement MQ consumer if needed
    public void onMessage(Object message) {
        log.info("[DownloadTaskConsumer] Received MQ message message={}", message);
        //todo
        //downloadTaskHandler.handle(payload);

    }
}

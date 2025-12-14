package com.seeho.downloadcenter.domain.dotask.process;

// TODO: RocketMQ integration removed - need to implement MQ consumer if needed
// import com.seeho.downloadcenter.constants.mq.MQConsumerGroupConstants;
// import com.seeho.downloadcenter.constants.mq.MQTopicConstants;
// TODO: RocketMQ integration removed - need to implement MQ consumer if needed
// import com.seeho.messiah.core.model.MessiahMessage;
// import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
// import org.apache.rocketmq.spring.core.RocketMQListener;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Placeholder MQ consumer that delegates tasks to {@link DownloadTaskHandler}.
 * RocketMQ integration is currently disabled, the bean stays for future wiring.
 */
@Slf4j
// TODO: RocketMQ integration removed
// @RocketMQMessageListener(topic = MQTopicConstants.DOWNLOAD_TASK_TOPIC,
//         consumerGroup = MQConsumerGroupConstants.DOWNLOAD_LOG_TASK_GROUP
// )
@Component
// TODO: RocketMQ integration removed
// @ConditionalOnProperty(name = "download.task.queue.mode", havingValue = "mq", matchIfMissing = true)
public class DownloadTaskConsumer {
    // TODO: RocketMQ integration removed - need to implement MQ consumer if needed
    // implements RocketMQListener<MessiahMessage<DownloadLogPO>> {

    @Resource
    private DownloadTaskHandler downloadTaskHandler;

    // TODO: RocketMQ integration removed - need to implement MQ consumer if needed
    /*
    @Override
    public void onMessage(MessiahMessage<DownloadLogPO> message) {
        log.info("[DownloadTaskConsumer] Received MQ message messageKey={}", message.getKey());

        DownloadLogPO payload = message.getPayload();
        try {
            // Delegate to the unified handler
            downloadTaskHandler.handle(payload);

        } catch (BusinessException e) {
            // Business issues: log and ACK to avoid useless retries
            log.error("[DownloadTaskConsumer] Business error, will ACK. taskId={}", payload.getId(), e);

        } catch (Exception e) {
            // System errors: throw to trigger retry
            log.error("[DownloadTaskConsumer] System error, will retry. taskId={}", payload.getId(), e);
            throw new RuntimeException("Export failed, will retry", e);
        }
    }
    */
}

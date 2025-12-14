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
 * 下载任务 MQ 消费者（适配层）
 * <p>
 * TODO: RocketMQ integration removed - this class is currently disabled.
 * Previously: 仅负责从 RocketMQ 拉取消息，委托给 DownloadTaskHandler 处理业务逻辑
 * </p>
 *
 * @author Leonpo
 * @since 2025-11-26
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
            // 委托给统一的业务处理器
            downloadTaskHandler.handle(payload);

        } catch (BusinessException e) {
            // 业务异常：记录日志，正常 ACK（避免无意义重试）
            log.error("[DownloadTaskConsumer] Business error, will ACK. taskId={}", payload.getId(), e);
            // 正常返回，消息 ACK

        } catch (Exception e) {
            // 系统异常：记录日志，抛出异常触发重试
            log.error("[DownloadTaskConsumer] System error, will retry. taskId={}", payload.getId(), e);
            throw new RuntimeException("Export failed, will retry", e);
        }
    }
    */
}

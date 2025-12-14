package com.seeho.downloadcenter.domain.dotask.sendtask.impl;

import com.seeho.downloadcenter.base.exception.BusinessException;
import com.seeho.downloadcenter.base.enums.DownloadRefServiceEnum;
import com.seeho.downloadcenter.domain.dotask.sendtask.SendTaskToMQService;
import com.seeho.downloadcenter.persistence.po.DownloadLogPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * 基于内存队列的任务发送服务（开发环境）
 * <p>
 * 使用 BlockingQueue + ScheduledExecutorService 模拟 MQ 的异步与延时能力
 * </p>
 *
 * @author Leonpo
 * @since 2025-11-26
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "download.task.queue.mode", havingValue = "memory")
public class InMemoryTaskQueueService implements SendTaskToMQService {

    /**
     * 任务队列（有界队列，支持背压）
     */
    private final BlockingQueue<DownloadLogPO> taskQueue;

    /**
     * 延时调度器（用于模拟延时消息）
     */
    private final ScheduledExecutorService delayScheduler;

    /**
     * 队列容量（默认1000，可配置）
     */
    @Value("${download.task.queue.capacity:1000}")
    private int queueCapacity;

    /**
     * 投递超时时间（秒，默认5秒）
     */
    private static final int OFFER_TIMEOUT_SECONDS = 5;

    public InMemoryTaskQueueService() {
        // 初始化有界队列（容量将在 @PostConstruct 后重建）
        this.taskQueue = new LinkedBlockingQueue<>(1000);

        // 初始化延时调度器（单线程即可，仅负责延时投递）
        this.delayScheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r, "InMemory-DelayScheduler");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 获取任务队列（供消费者使用）
     */
    public BlockingQueue<DownloadLogPO> getTaskQueue() {
        return taskQueue;
    }

    @Override
    public void sendTaskToMQ(DownloadLogPO downloadLogPO) {
        log.info("[InMemoryTaskQueue] Send task to memory queue, taskId={}, messageKey={}",
                downloadLogPO.getId(), downloadLogPO.getMessageKey());

        // 获取延时时间（秒）
        Long timeoutSeconds = DownloadRefServiceEnum.matchDownloadType(downloadLogPO.getDownloadType()).getTimeout();

        if (timeoutSeconds == null || timeoutSeconds <= 0) {
            // 无延时，直接投递
            offerToQueue(downloadLogPO);
        } else {
            // 有延时，使用 ScheduledExecutorService 延时投递
            delayScheduler.schedule(() -> offerToQueue(downloadLogPO), timeoutSeconds, TimeUnit.SECONDS);
            log.info("[InMemoryTaskQueue] Task scheduled with delay, taskId={}, delaySeconds={}",
                    downloadLogPO.getId(), timeoutSeconds);
        }
    }

    /**
     * 投递任务到队列
     */
    private void offerToQueue(DownloadLogPO task) {
        try {
            boolean offered = taskQueue.offer(task, OFFER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!offered) {
                log.error("[InMemoryTaskQueue] Queue is full, task rejected. taskId={}", task.getId());
                throw new BusinessException("开发环境：任务队列已满，请稍后重试");
            }
            log.info("[InMemoryTaskQueue] Task offered to queue successfully, taskId={}, queueSize={}",
                    task.getId(), taskQueue.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[InMemoryTaskQueue] Interrupted while offering task, taskId={}", task.getId(), e);
            throw new BusinessException("任务投递被中断");
        }
    }

    /**
     * 优雅关闭（Spring 容器销毁时调用）
     */
    public void shutdown() {
        log.info("[InMemoryTaskQueue] Shutting down delay scheduler...");
        delayScheduler.shutdown();
        try {
            if (!delayScheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                delayScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            delayScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("[InMemoryTaskQueue] Delay scheduler shutdown completed");
    }
}

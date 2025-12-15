package com.seeho.downloadcenter.domain.dotask.sendtask.impl;

import com.seeho.downloadcenter.base.exception.BusinessException;
import com.seeho.downloadcenter.base.enums.DownloadRefServiceEnum;
import com.seeho.downloadcenter.domain.dotask.sendtask.SendTaskToMQService;
import com.seeho.downloadcenter.persistence.po.DownloadLogPO;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * Development-only task queue backed by {@link BlockingQueue} plus simple delay scheduling.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "download.task.queue.mode", havingValue = "memory")
public class InMemoryTaskQueueService implements SendTaskToMQService {

    private final BlockingQueue<DownloadLogPO> taskQueue;

    private final ScheduledExecutorService delayScheduler;

    @Value("${download.task.queue.capacity:1000}")
    private int queueCapacity;

    private static final int OFFER_TIMEOUT_SECONDS = 5;

    public InMemoryTaskQueueService() {
        this.taskQueue = new LinkedBlockingQueue<>(1000);

        this.delayScheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r, "InMemory-DelayScheduler");
            thread.setDaemon(true);
            return thread;
        });
    }

    public BlockingQueue<DownloadLogPO> getTaskQueue() {
        return taskQueue;
    }

    @Override
    public void sendTaskToMQ(DownloadLogPO downloadLogPO) {
        log.info("[InMemoryTaskQueue] Send task to memory queue, taskId={}, messageKey={}",
                downloadLogPO.getId(), downloadLogPO.getMessageKey());

        Long timeoutSeconds = DownloadRefServiceEnum.matchDownloadType(downloadLogPO.getDownloadType()).getTimeout();

        if (timeoutSeconds == null || timeoutSeconds <= 0) {
            offerToQueue(downloadLogPO);
        } else {
            delayScheduler.schedule(() -> offerToQueue(downloadLogPO), timeoutSeconds, TimeUnit.SECONDS);
            log.info("[InMemoryTaskQueue] Task scheduled with delay, taskId={}, delaySeconds={}",
                    downloadLogPO.getId(), timeoutSeconds);
        }
    }

    private void offerToQueue(DownloadLogPO task) {
        try {
            boolean offered = taskQueue.offer(task, OFFER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!offered) {
                log.error("[InMemoryTaskQueue] Queue is full, task rejected. taskId={}", task.getId());
                throw new BusinessException("Task queue is full, please retry");
            }
            log.info("[InMemoryTaskQueue] Task offered to queue successfully, taskId={}, queueSize={}",
                    task.getId(), taskQueue.size());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[InMemoryTaskQueue] Interrupted while offering task, taskId={}", task.getId(), e);
            throw new BusinessException("Task offer interrupted");
        }
    }

    @PreDestroy
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

package com.seeho.downloadcenter.domain.dotask.process.consumer;

import com.seeho.downloadcenter.base.exception.BusinessException;
import com.seeho.downloadcenter.domain.dotask.process.DownloadTaskHandler;
import com.seeho.downloadcenter.domain.dotask.sendtask.impl.InMemoryTaskQueueService;
import com.seeho.downloadcenter.persistence.po.DownloadLogPO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * In-memory task consumer used for development profiles.
 * Pulls tasks from a blocking queue and delegates them to {@link DownloadTaskHandler}.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "download.task.queue.mode", havingValue = "memory")
public class InMemoryTaskConsumer implements InitializingBean, DisposableBean {

    @Resource
    private InMemoryTaskQueueService queueService;

    @Resource
    private DownloadTaskHandler downloadTaskHandler;

    private ExecutorService consumerExecutor;

    @Value("${download.task.consumer.thread-count:3}")
    private int consumerThreadCount;

    private volatile boolean running = true;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("[InMemoryTaskConsumer] Initializing consumer with {} threads", consumerThreadCount);

        this.consumerExecutor = new ThreadPoolExecutor(
                consumerThreadCount,
                consumerThreadCount,
                0L,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                new ThreadFactory() {
                    private int count = 0;

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "InMemory-TaskConsumer-" + (++count));
                        thread.setDaemon(false);
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        for (int i = 0; i < consumerThreadCount; i++) {
            consumerExecutor.submit(this::consumeLoop);
        }

        log.info("[InMemoryTaskConsumer] Consumer started with {} threads", consumerThreadCount);
    }

    private void consumeLoop() {
        log.info("[InMemoryTaskConsumer] Consumer thread started, threadName={}", Thread.currentThread().getName());

        BlockingQueue<DownloadLogPO> taskQueue = queueService.getTaskQueue();

        while (running) {
            try {
                DownloadLogPO task = taskQueue.poll(1, TimeUnit.SECONDS);

                if (task == null) {
                    continue;
                }

                log.info("[InMemoryTaskConsumer] Received task from memory queue, taskId={}, messageKey={}, threadName={}",
                        task.getId(), task.getMessageKey(), Thread.currentThread().getName());
                downloadTaskHandler.handle(task);
            } catch (InterruptedException e) {
                log.warn("[InMemoryTaskConsumer] Consumer thread interrupted, threadName={}", Thread.currentThread().getName());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("[InMemoryTaskConsumer] Unexpected error in consume loop, threadName={}", Thread.currentThread().getName(), e);
            }
        }

        log.info("[InMemoryTaskConsumer] Consumer thread stopped, threadName={}", Thread.currentThread().getName());
    }

    @Override
    public void destroy() throws Exception {
        log.info("[InMemoryTaskConsumer] Shutting down consumer...");

        running = false;

        consumerExecutor.shutdown();
        try {
            if (!consumerExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                log.warn("[InMemoryTaskConsumer] Consumer threads did not terminate in time, forcing shutdown...");
                consumerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("[InMemoryTaskConsumer] Interrupted while waiting for consumer shutdown", e);
            consumerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("[InMemoryTaskConsumer] Consumer shutdown completed");
    }
}

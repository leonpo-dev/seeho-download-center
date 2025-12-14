package com.seeho.downloadcenter.domain.dotask.process;

import com.seeho.downloadcenter.base.exception.BusinessException;
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
 * 基于内存队列的任务消费者（开发环境）
 * <p>
 * 从 BlockingQueue 拉取任务并委托给 DownloadTaskHandler 处理
 * 使用固定线程池（IO 密集型配置），支持并发消费
 * </p>
 *
 * @author Leonpo
 * @since 2025-11-26
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "download.task.queue.mode", havingValue = "memory")
public class InMemoryTaskConsumer implements InitializingBean, DisposableBean {

    @Resource
    private InMemoryTaskQueueService queueService;

    @Resource
    private DownloadTaskHandler downloadTaskHandler;

    /**
     * 消费者线程池（IO 密集型配置）
     */
    private ExecutorService consumerExecutor;

    /**
     * 消费者线程数（默认3，可配置）
     * IO 密集型推荐：核心数 * 2 或更多，这里默认 3
     */
    @Value("${download.task.consumer.thread-count:3}")
    private int consumerThreadCount;

    /**
     * 停止标志
     */
    private volatile boolean running = true;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("[InMemoryTaskConsumer] Initializing consumer with {} threads", consumerThreadCount);

        // 创建固定大小线程池（IO 密集型配置）
        // 核心线程数 = 最大线程数 = consumerThreadCount
        // 使用 SynchronousQueue（直接交接，无缓冲）
        // 拒绝策略：CallerRunsPolicy（降级到调用线程执行）
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
                        thread.setDaemon(false); // 非守护线程，确保任务执行完毕
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时降级到调用线程
        );

        // 启动消费者线程
        for (int i = 0; i < consumerThreadCount; i++) {
            consumerExecutor.submit(this::consumeLoop);
        }

        log.info("[InMemoryTaskConsumer] Consumer started with {} threads", consumerThreadCount);
    }

    /**
     * 消费循环（每个线程执行）
     */
    private void consumeLoop() {
        log.info("[InMemoryTaskConsumer] Consumer thread started, threadName={}", Thread.currentThread().getName());

        BlockingQueue<DownloadLogPO> taskQueue = queueService.getTaskQueue();

        while (running) {
            try {
                // 从队列拉取任务（阻塞等待，超时 1 秒）
                DownloadLogPO task = taskQueue.poll(1, TimeUnit.SECONDS);

                if (task == null) {
                    // 超时无任务，继续下一轮
                    continue;
                }

                log.info("[InMemoryTaskConsumer] Received task from memory queue, taskId={}, messageKey={}, threadName={}",
                        task.getId(), task.getMessageKey(), Thread.currentThread().getName());

                // 委托给业务处理器
                try {
                    downloadTaskHandler.handle(task);
                    log.info("[InMemoryTaskConsumer] Task processed successfully, taskId={}", task.getId());

                } catch (BusinessException e) {
                    // 业务异常：记录日志，继续下一个任务（不重试）
                    log.error("[InMemoryTaskConsumer] Business error, skip task. taskId={}", task.getId(), e);

                } catch (Exception e) {
                    // 系统异常：记录日志，继续下一个任务（开发环境不重试，等 job 兜底）
                    log.error("[InMemoryTaskConsumer] System error, skip task. taskId={}", task.getId(), e);
                }

            } catch (InterruptedException e) {
                log.warn("[InMemoryTaskConsumer] Consumer thread interrupted, threadName={}", Thread.currentThread().getName());
                Thread.currentThread().interrupt();
                break; // 中断时退出循环
            } catch (Exception e) {
                log.error("[InMemoryTaskConsumer] Unexpected error in consume loop, threadName={}", Thread.currentThread().getName(), e);
                // 继续下一轮，避免线程退出
            }
        }

        log.info("[InMemoryTaskConsumer] Consumer thread stopped, threadName={}", Thread.currentThread().getName());
    }

    @Override
    public void destroy() throws Exception {
        log.info("[InMemoryTaskConsumer] Shutting down consumer...");

        // 设置停止标志
        running = false;

        // 关闭线程池
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

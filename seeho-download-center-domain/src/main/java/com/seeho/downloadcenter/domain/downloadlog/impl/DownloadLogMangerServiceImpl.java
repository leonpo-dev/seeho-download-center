package com.seeho.downloadcenter.domain.downloadlog.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seeho.downloadcenter.base.constants.JobConstants;
import com.seeho.downloadcenter.base.constants.UserContext;
import com.seeho.downloadcenter.base.enums.DownloadStatusEnum;
import com.seeho.downloadcenter.base.common.PageResult;
import com.seeho.downloadcenter.domain.dotask.sendtask.SendTaskToMQService;
import com.seeho.downloadcenter.domain.downloadlog.DownloadLogMangerService;
import com.seeho.downloadcenter.base.model.JobQueryDTO;
import com.seeho.downloadcenter.domain.utils.BeanUtil;
import com.seeho.downloadcenter.domain.utils.CollectionUtils;
import com.seeho.downloadcenter.domain.utils.JsonUtil;
import com.seeho.downloadcenter.domain.utils.MessageKeyUtils;
import com.seeho.downloadcenter.base.model.DownloadListDTO;
import com.seeho.downloadcenter.base.model.DownloadLogDTO;
import com.seeho.downloadcenter.base.model.QueryDownloadDTO;
import com.seeho.downloadcenter.persistence.IService.DownloadLogService;
import com.seeho.downloadcenter.persistence.po.DownloadLogPO;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Domain service that orchestrates download task lifecycle.
 */
@Slf4j
@Service
public class DownloadLogMangerServiceImpl implements DownloadLogMangerService {

    @Resource
    private DownloadLogService downloadLogService;

    @Resource
    private SendTaskToMQService sendTaskToMQService;

    /**
     * Persists a new download task and triggers the async pipeline.
     */
    @Transactional
    @Override
    public Long createExportTask(DownloadLogDTO downloadLogDTO) {
        // TODO replace the demo user ID once authentication is wired.
        downloadLogDTO.setUserId(UserContext.userId);
        DownloadLogPO downloadLogPO = BeanUtil.copy(downloadLogDTO, DownloadLogPO.class);
        downloadLogPO.setTitles(JsonUtil.toJson(downloadLogDTO.getTitles()));
        downloadLogPO.setDownloadType(downloadLogDTO.getDownloadType().getDlCode());

        downloadLogPO.setDownloadStatus(DownloadStatusEnum.NOT_EXECUTED.getCode());

        downloadLogPO.setCreateUserId(UserContext.userId);
        downloadLogPO.setUpdateUserId(UserContext.userId);

        downloadLogService.save(downloadLogPO);
        Long logPOId = downloadLogPO.getId();

        String messageKey = MessageKeyUtils.generate(downloadLogDTO.getDownloadType().getDlCode(), logPOId);
        downloadLogPO.setMessageKey(messageKey);
        downloadLogService.updateById(downloadLogPO);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    log.info("[ExportTask] Transaction committed, sending MQ message taskId={} messageKey={}", logPOId,
                            messageKey);
                    sendTaskToMQService.sendTaskToMQ(downloadLogPO);
                    log.info("[ExportTask] MQ message sent successfully taskId={}", logPOId);
                } catch (Exception e) {
                    log.error("[ExportTask] Failed to send MQ message (unknown exception) taskId={} messageKey={}",
                            logPOId, messageKey, e);
                    updateTaskStatusToSendFailed(logPOId, e.getMessage());
                }
            }
        });

        return logPOId;
    }

    private void updateTaskStatusToSendFailed(Long taskId, String errorMessage) {
        try {
            DownloadLogPO updatePO = new DownloadLogPO();
            updatePO.setId(taskId);
            updatePO.setDownloadStatus(DownloadStatusEnum.SEND_FAILED.getCode());
            updatePO.setFailReason("Message send failed: " + errorMessage);
            downloadLogService.updateById(updatePO);
            log.info("[ExportTask] Task status updated to SEND_FAILED taskId={}", taskId);
        } catch (Exception ex) {
            log.error("[ExportTask] Failed to update task status taskId={}", taskId, ex);
        }
    }

    @Override
    public PageResult<DownloadListDTO> queryExportTask(QueryDownloadDTO queryDownloadDTO) {
        LambdaQueryWrapper<DownloadLogPO> queryWrapper = buildQueryWrapper(queryDownloadDTO);

        Page<DownloadLogPO> page = new Page<>(queryDownloadDTO.getPageIndex(), queryDownloadDTO.getPageSize());
        IPage<DownloadLogPO> pageResult = downloadLogService.page(page, queryWrapper);

        List<DownloadListDTO> dtoList = BeanUtil.copyList(pageResult.getRecords(), DownloadListDTO.class);

        return PageResult.of(dtoList, pageResult.getTotal(), queryDownloadDTO.getPageIndex(),
                queryDownloadDTO.getPageSize());
    }

    @Override
    public DownloadLogPO queryDownloadLogByMessageKey(String messageKey) {
        LambdaQueryWrapper<DownloadLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLogPO::getMessageKey, messageKey);
        return downloadLogService.getOne(wrapper);
    }

    @Override
    public Boolean updateDownloadLogStatus(Long id, DownloadStatusEnum newStatus, DownloadStatusEnum desiredStatus) {
        // TODO reintroduce userId check once we have tenant info.
        LambdaUpdateWrapper<DownloadLogPO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DownloadLogPO::getId, id)
                .eq(DownloadLogPO::getDownloadStatus, desiredStatus.getCode())
                .set(DownloadLogPO::getDownloadStatus, newStatus.getCode());
        return downloadLogService.update(updateWrapper);
    }

    @Override
    public Boolean updateDLPOById(DownloadLogPO downloadLogPO) {
        Assert.notNull(downloadLogPO.getId(), "Download log ID cannot be null");
        downloadLogPO.setUserId(UserContext.userId);
        return downloadLogService.updateById(downloadLogPO);
    }

    @Override
    public Boolean checkFileExist(String fileUrl) {
        LambdaQueryWrapper<DownloadLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLogPO::getUserId, UserContext.userId);
        wrapper.eq(DownloadLogPO::getFileUrl, fileUrl);
        return downloadLogService.exists(wrapper);
    }

    @Override
    public DownloadLogPO queryTaskById(Long logId) {
        LambdaQueryWrapper<DownloadLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLogPO::getUserId, UserContext.userId);
        wrapper.eq(DownloadLogPO::getId, logId);
        return downloadLogService.getOne(wrapper);
    }

    @Override
    public List<DownloadLogPO> queryToTaskList(JobQueryDTO jobQueryDTO) {
        LambdaQueryWrapper<DownloadLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Objects.nonNull(jobQueryDTO.getStartTime()), DownloadLogPO::getCreateTime,
                jobQueryDTO.getStartTime());
        wrapper.le(Objects.nonNull(jobQueryDTO.getEndTime()), DownloadLogPO::getCreateTime, jobQueryDTO.getEndTime());
        wrapper.in(CollectionUtils.isNotEmpty(jobQueryDTO.getStatusList()), DownloadLogPO::getDownloadStatus,
                jobQueryDTO.getStatusList().stream().map(DownloadStatusEnum::getCode).toList());
        if (wrapper.isEmptyOfWhere()) {
            return List.of();
        }
        wrapper.le(DownloadLogPO::getRetryCount, JobConstants.DEFAULT_RETRY_COUNT);
        return downloadLogService.list(wrapper);
    }

    @Transactional
    @Override
    public void tryDoTaskAgain(DownloadLogPO task) {
        LambdaQueryWrapper<DownloadLogPO> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(DownloadLogPO::getId, task.getId());
        updateWrapper.eq(DownloadLogPO::getDownloadStatus, task.getDownloadStatus());

        String messageKey = MessageKeyUtils.generate(task.getDownloadType(), task.getId());
        task.setMessageKey(messageKey);

        task.setDownloadStatus(DownloadStatusEnum.NOT_EXECUTED.getCode());
        task.setRetryCount(task.getRetryCount() + 1);
        downloadLogService.update(task, updateWrapper);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    log.info("[tryDoTaskAgain] Transaction committed, sending MQ message taskId={} messageKey={}",
                            task.getId(), messageKey);
                    sendTaskToMQService.sendTaskToMQ(task);
                    log.info("[tryDoTaskAgain] MQ message sent successfully taskId={}", task.getId());
                } catch (Exception e) {
                    log.error("[tryDoTaskAgain] Failed to send MQ message (unknown exception) taskId={} messageKey={}",
                            task.getId(), messageKey, e);
                    updateTaskStatusToSendFailed(task.getId(), e.getMessage());
                }
            }
        });
    }

    private LambdaQueryWrapper<DownloadLogPO> buildQueryWrapper(QueryDownloadDTO queryDTO) {
        LambdaQueryWrapper<DownloadLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLogPO::getUserId, UserContext.userId);
        wrapper.like(StringUtils.hasText(queryDTO.getDownloadName()), DownloadLogPO::getDownloadName,
                queryDTO.getDownloadName());

        wrapper.eq(Objects.nonNull(queryDTO.getDownloadStatus()), DownloadLogPO::getDownloadStatus,
                queryDTO.getDownloadStatus());

        LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);
        wrapper.ge(DownloadLogPO::getCreateTime, tenDaysAgo);

        wrapper.orderByDesc(DownloadLogPO::getCreateTime);

        return wrapper;
    }

}

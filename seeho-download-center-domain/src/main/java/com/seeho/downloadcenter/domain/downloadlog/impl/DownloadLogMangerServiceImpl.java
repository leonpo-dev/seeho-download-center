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
 * @author Leonpo
 * @since 2025-11-25
 */
@Slf4j
@Service
public class DownloadLogMangerServiceImpl implements DownloadLogMangerService {

    @Resource
    private DownloadLogService downloadLogService;

    @Resource
    private SendTaskToMQService sendTaskToMQService;

    /**
     * 创建导出任务
     *
     * @param downloadLogDTO 下载日志DTO
     * @return 创建的任务ID
     */
    @Transactional
    @Override
    public Long createExportTask(DownloadLogDTO downloadLogDTO) {
        // TODO: you UserContext userid
        downloadLogDTO.setUserId(UserContext.userId);
        // DTO转PO
        DownloadLogPO downloadLogPO = BeanUtil.copy(downloadLogDTO, DownloadLogPO.class);
        // titles 对象序列化为 JSON 存入 PO
        downloadLogPO.setTitles(JsonUtil.toJson(downloadLogDTO.getTitles()));
        downloadLogPO.setDownloadType(downloadLogDTO.getDownloadType().getDlCode());

        // 设置初始状态：未执行
        downloadLogPO.setDownloadStatus(DownloadStatusEnum.NOT_EXECUTED.getCode());

        // 设置创建人和修改人ID
        downloadLogPO.setCreateUserId(UserContext.userId);
        downloadLogPO.setUpdateUserId(UserContext.userId);

        // 保存到数据库
        downloadLogService.save(downloadLogPO);
        Long logPOId = downloadLogPO.getId();

        // 生成messageKey并回填
        String messageKey = MessageKeyUtils.generate(downloadLogDTO.getDownloadType().getDlCode(), logPOId);
        downloadLogPO.setMessageKey(messageKey);
        downloadLogService.updateById(downloadLogPO);

        // 注册事务提交后回调：发送MQ消息
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
                    // 更新任务状态为发送失败
                    updateTaskStatusToSendFailed(logPOId, e.getMessage());
                }
            }
        });

        // 返回创建的任务ID
        return logPOId;
    }

    /**
     * 更新任务状态为发送失败
     *
     * @param taskId       任务ID
     * @param errorMessage 错误信息
     */
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

    /**
     * 查询导出任务列表
     *
     * @param queryDownloadDTO 查询参数
     * @return 导出任务列表
     */
    @Override
    public PageResult<DownloadListDTO> queryExportTask(QueryDownloadDTO queryDownloadDTO) {
        // 构建查询条件
        LambdaQueryWrapper<DownloadLogPO> queryWrapper = buildQueryWrapper(queryDownloadDTO);

        // 分页查询
        Page<DownloadLogPO> page = new Page<>(queryDownloadDTO.getPageIndex(), queryDownloadDTO.getPageSize());
        IPage<DownloadLogPO> pageResult = downloadLogService.page(page, queryWrapper);

        // PO转DTO
        List<DownloadListDTO> dtoList = BeanUtil.copyList(pageResult.getRecords(), DownloadListDTO.class);

        // 构建分页结果
        return PageResult.of(dtoList, pageResult.getTotal(), queryDownloadDTO.getPageIndex(),
                queryDownloadDTO.getPageSize());
    }

    @Override
    public DownloadLogPO queryDownloadLogByMessageKey(String messageKey) {
        LambdaQueryWrapper<DownloadLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLogPO::getMessageKey, messageKey);
        return downloadLogService.getOne(wrapper);
    }

    /**
     * 更新下载日志状态
     *
     * @param id            下载日志ID
     * @param newStatus     新的状态
     * @param desiredStatus 期望的状态
     * @return 更新记录数
     */
    @Override
    public Boolean updateDownloadLogStatus(Long id, DownloadStatusEnum newStatus, DownloadStatusEnum desiredStatus) {
        // TODO: UserContext removed - skip userId check for now
        LambdaUpdateWrapper<DownloadLogPO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DownloadLogPO::getId, id)
                .eq(DownloadLogPO::getDownloadStatus, desiredStatus.getCode())
                .set(DownloadLogPO::getDownloadStatus, newStatus.getCode());
        // TODO: UserContext removed - userId check removed
        return downloadLogService.update(updateWrapper);
    }

    /**
     * 更新DLPO
     *
     * @param downloadLogPO
     * @return
     */
    @Override
    public Boolean updateDLPOById(DownloadLogPO downloadLogPO) {
        // 参数校验
        Assert.notNull(downloadLogPO.getId(), "Download log ID cannot be null");
        downloadLogPO.setUserId(UserContext.userId);
        // MyBatis-Plus 默认只更新非 null 字段
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

        // 生成messageKey
        String messageKey = MessageKeyUtils.generate(task.getDownloadType(), task.getId());
        task.setMessageKey(messageKey);

        task.setDownloadStatus(DownloadStatusEnum.NOT_EXECUTED.getCode());
        task.setRetryCount(task.getRetryCount() + 1);
        downloadLogService.update(task, updateWrapper);

        // 注册事务提交后回调：发送MQ消息重新调起任务
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
                    // 更新任务状态为发送失败
                    updateTaskStatusToSendFailed(task.getId(), e.getMessage());
                }
            }
        });
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<DownloadLogPO> buildQueryWrapper(QueryDownloadDTO queryDTO) {
        LambdaQueryWrapper<DownloadLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadLogPO::getUserId, UserContext.userId);
        // 下载名称（模糊查询）
        wrapper.like(StringUtils.hasText(queryDTO.getDownloadName()), DownloadLogPO::getDownloadName,
                queryDTO.getDownloadName());

        // 下载状态
        wrapper.eq(Objects.nonNull(queryDTO.getDownloadStatus()), DownloadLogPO::getDownloadStatus,
                queryDTO.getDownloadStatus());

        // ，默认查询近10天的数据
        LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);
        wrapper.ge(DownloadLogPO::getCreateTime, tenDaysAgo);

        // 按创建时间倒序排序
        wrapper.orderByDesc(DownloadLogPO::getCreateTime);

        return wrapper;
    }

}

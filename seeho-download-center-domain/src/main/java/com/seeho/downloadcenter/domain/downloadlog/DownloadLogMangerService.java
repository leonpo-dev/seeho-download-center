package com.seeho.downloadcenter.domain.downloadlog;

import com.seeho.downloadcenter.base.common.PageResult;
import com.seeho.downloadcenter.base.enums.DownloadStatusEnum;
import com.seeho.downloadcenter.base.model.JobQueryDTO;
import com.seeho.downloadcenter.base.model.DownloadListDTO;
import com.seeho.downloadcenter.base.model.DownloadLogDTO;
import com.seeho.downloadcenter.base.model.QueryDownloadDTO;
import com.seeho.downloadcenter.persistence.po.DownloadLogPO;

import java.util.List;

public interface DownloadLogMangerService {

    /**
     * 创建导出任务
     *
     * @param downloadLogDTO 下载日志DTO
     * @return 创建的任务ID
     */
    Long createExportTask(DownloadLogDTO downloadLogDTO);

    /**
     * 查询导出任务列表
     *
     * @param queryDownloadDTO 查询参数
     * @return 导出任务列表
     */
    PageResult<DownloadListDTO> queryExportTask(QueryDownloadDTO queryDownloadDTO);


    /**
     * 根据messageKey查询下载日志
     *
     * @param messageKey 消息Key
     * @return 下载日志
     */
    DownloadLogPO queryDownloadLogByMessageKey(String messageKey);

    /**
     * 更新下载日志状态
     *
     * @param id            下载日志ID
     * @param newStatus     新状态
     * @param desiredStatus 期望状态
     * @return 更新结果
     */
    Boolean updateDownloadLogStatus(Long id, DownloadStatusEnum newStatus, DownloadStatusEnum desiredStatus);


    /**
     * 更新下载日志
     *
     * @param downloadLogPO
     * @return
     */
    Boolean updateDLPOById(DownloadLogPO downloadLogPO);

    Boolean checkFileExist(String filename);

    DownloadLogPO queryTaskById(Long logId);

    /**
     * 查询任务列表
     * @param jobQueryDTO job查询参数
     * @return
     */
    List<DownloadLogPO> queryToTaskList(JobQueryDTO jobQueryDTO);

    /**
     * 尝试再次执行任务
     * @param task
     */
    void tryDoTaskAgain(DownloadLogPO task);
}

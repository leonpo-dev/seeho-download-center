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

    /** Creates a new download/export task. */
    Long createExportTask(DownloadLogDTO downloadLogDTO);

    /** Queries paged download task list for the current user. */
    PageResult<DownloadListDTO> queryExportTask(QueryDownloadDTO queryDownloadDTO);


    /** Looks up a log entry by the MQ message key. */
   DownloadLogPO queryDownloadLogByMessageKey(String messageKey);

    /** Updates a task status with optimistic checking. */
    Boolean updateDownloadLogStatus(Long id, DownloadStatusEnum newStatus, DownloadStatusEnum desiredStatus);


    /** Partially updates a log entry by ID. */
    Boolean updateDLPOById(DownloadLogPO downloadLogPO);

    Boolean checkFileExist(String filename);

    DownloadLogPO queryTaskById(Long logId);

    /** Queries tasks to refill the job queue. */
    List<DownloadLogPO> queryToTaskList(JobQueryDTO jobQueryDTO);

    /** Re-enqueues a failed task. */
    void tryDoTaskAgain(DownloadLogPO task);
}

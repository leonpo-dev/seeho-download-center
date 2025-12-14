package com.seeho.downloadcenter.controller;

import com.seeho.downloadcenter.base.common.Response;
import com.seeho.downloadcenter.base.common.PageResult;
import com.seeho.downloadcenter.domain.utils.BeanUtil;
import org.springframework.util.Assert;
import com.seeho.downloadcenter.base.enums.DownloadRefServiceEnum;
import com.seeho.downloadcenter.base.enums.DownloadStatusEnum;
import com.seeho.downloadcenter.domain.downloadlog.DownloadLogMangerService;
import com.seeho.downloadcenter.utils.FileUtils;
import com.seeho.downloadcenter.base.model.DownloadListDTO;
import com.seeho.downloadcenter.base.model.DownloadLogDTO;
import com.seeho.downloadcenter.base.model.QueryDownloadDTO;
import com.seeho.downloadcenter.persistence.po.DownloadLogPO;
import com.seeho.downloadcenter.request.DownloadLogRequest;
import com.seeho.downloadcenter.request.QueryDownloadRequest;
import com.seeho.downloadcenter.vo.DownloadLogVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Leonpo
 * @since 2025-11-25
 */
@Slf4j
@RestController
public class DownloadCenterController {

    @Resource
    private DownloadLogMangerService downloadLogMangerService;

    /**
     * 生成导出数据任务记录
     *
     * @param request
     */
    @PostMapping("/createExportTask")
    public Response<Long> createExportTask(@RequestBody @Validated DownloadLogRequest request) {
        DownloadRefServiceEnum downloadEnum = DownloadRefServiceEnum.matchDownloadType(request.getDownloadEnum());
        DownloadLogDTO copy = BeanUtil.copy(request, DownloadLogDTO.class);
        copy.setDownloadType(downloadEnum);
        Long taskId = downloadLogMangerService.createExportTask(copy);
        return Response.success(taskId);
    }

    /**
     * 查询导出数据任务记录
     *
     * @param request
     * @return
     */
    @PostMapping("/queryExportTask")
    public Response<PageResult<DownloadLogVO>> queryExportTask(@RequestBody @Validated QueryDownloadRequest request) {
        PageResult<DownloadListDTO> pageResult = downloadLogMangerService.queryExportTask(BeanUtil.copy(request, QueryDownloadDTO.class));
        // 手动转换列表
        List<DownloadLogVO> voList = BeanUtil.copyList(pageResult.getRecords(), DownloadLogVO.class);
        PageResult<DownloadLogVO> voPageResult = PageResult.of(voList, pageResult.getTotal(), pageResult.getPageIndex(), pageResult.getPageSize());
        return Response.success(voPageResult);
    }

    /**
     * 下载文件
     *
     * @param logId
     * @param response
     */
    @GetMapping("/download/{logId}")
    public void downloadFile(@PathVariable(name = "logId") Long logId, HttpServletResponse response) {
        DownloadLogPO taskById = downloadLogMangerService.queryTaskById(logId);
        Assert.notNull(taskById, "下载任务不存在");
        Assert.hasText(taskById.getFileUrl(), "文件不存在");
        // 下载文件
        FileUtils.downloadFile(taskById.getFileUrl(), response);
    }

    /**
     * 取消任务
     *
     * @param logId
     * @return
     */
    @PostMapping("/cancelTask/{logId}")
    public Response<Void> cancelTask(@PathVariable(name = "logId") Long logId) {
        Boolean updated = downloadLogMangerService.updateDownloadLogStatus(logId, DownloadStatusEnum.CANCELLED, DownloadStatusEnum.NOT_EXECUTED);
        Assert.isTrue(updated,"任务状态已变更，不能取消");
        return Response.success();
    }
}
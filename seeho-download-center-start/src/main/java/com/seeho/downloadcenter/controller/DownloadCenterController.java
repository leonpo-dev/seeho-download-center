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

@Slf4j
@RestController
public class DownloadCenterController {

    @Resource
    private DownloadLogMangerService downloadLogMangerService;

    @PostMapping("/createExportTask")
    public Response<Long> createExportTask(@RequestBody @Validated DownloadLogRequest request) {
        DownloadRefServiceEnum downloadEnum = DownloadRefServiceEnum.matchDownloadType(request.getDownloadEnum());
        DownloadLogDTO copy = BeanUtil.copy(request, DownloadLogDTO.class);
        copy.setDownloadType(downloadEnum);
        Long taskId = downloadLogMangerService.createExportTask(copy);
        return Response.success(taskId);
    }

    @PostMapping("/queryExportTask")
    public Response<PageResult<DownloadLogVO>> queryExportTask(@RequestBody @Validated QueryDownloadRequest request) {
        PageResult<DownloadListDTO> pageResult = downloadLogMangerService.queryExportTask(BeanUtil.copy(request, QueryDownloadDTO.class));
        List<DownloadLogVO> voList = BeanUtil.copyList(pageResult.getRecords(), DownloadLogVO.class);
        PageResult<DownloadLogVO> voPageResult = PageResult.of(voList, pageResult.getTotal(), pageResult.getPageIndex(), pageResult.getPageSize());
        return Response.success(voPageResult);
    }

    @GetMapping("/download/{logId}")
    public void downloadFile(@PathVariable(name = "logId") Long logId, HttpServletResponse response) {
        DownloadLogPO taskById = downloadLogMangerService.queryTaskById(logId);
        Assert.notNull(taskById, "Download task not found");
        Assert.hasText(taskById.getFileUrl(), "File path not available");
        FileUtils.downloadFile(taskById.getFileUrl(), response);
    }

    @PostMapping("/cancelTask/{logId}")
    public Response<Void> cancelTask(@PathVariable(name = "logId") Long logId) {
        Boolean updated = downloadLogMangerService.updateDownloadLogStatus(logId, DownloadStatusEnum.CANCELLED, DownloadStatusEnum.NOT_EXECUTED);
        Assert.isTrue(updated,"Task status changed, cannot cancel");
        return Response.success();
    }
}

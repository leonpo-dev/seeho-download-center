package com.seeho.downloadcenter.base.model;

import com.seeho.downloadcenter.base.enums.DownloadStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobQueryDTO {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<DownloadStatusEnum> statusList;
}

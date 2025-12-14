package com.seeho.downloadcenter.base.model;

import com.seeho.downloadcenter.base.enums.DownloadRefServiceEnum;
import lombok.Data;

import java.util.List;

@Data
public class DownloadLogDTO {

    private Long userId;
    private String downloadName;
    private DownloadRefServiceEnum downloadType;
    private String downloadCondition;

    /** Dynamic column selection passed from the client side. */
    private List<DownloadColumnDTO> titles;
}

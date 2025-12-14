package com.seeho.downloadcenter.base.model;

import com.seeho.downloadcenter.base.common.PageRequest;
import lombok.Data;

@Data
public class QueryDownloadDTO extends PageRequest {

    private String downloadName;

    private Byte downloadStatus;
}

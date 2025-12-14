package com.seeho.downloadcenter.request;

import com.seeho.downloadcenter.base.common.PageRequest;
import lombok.Data;

@Data
public class QueryDownloadRequest extends PageRequest {

    private String downloadName;

    /** @see com.seeho.downloadcenter.base.enums.DownloadStatusEnum */
    private Byte downloadStatus;

}

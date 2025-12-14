package com.seeho.downloadcenter.request;

import com.seeho.downloadcenter.base.common.PageRequest;
import lombok.Data;

/**
 * @author Leonpo
 * @since 2025-11-25
 */
@Data
public class QueryDownloadRequest extends PageRequest {

    private String downloadName;

    /**
     * 下载状态
     *
     * @see com.seeho.downloadcenter.base.enums.DownloadStatusEnum
     */
    private Byte downloadStatus;

}

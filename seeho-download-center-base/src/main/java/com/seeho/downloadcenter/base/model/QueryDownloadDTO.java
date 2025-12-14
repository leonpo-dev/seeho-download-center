package com.seeho.downloadcenter.base.model;

import com.seeho.downloadcenter.base.common.PageRequest;
import lombok.Data;

/**
 * @author Leonpo
 * @since 2025-11-25
 */
@Data
public class QueryDownloadDTO extends PageRequest {

    private String downloadName;

    /**
     * 下载状态
     */
    private Byte downloadStatus;
}

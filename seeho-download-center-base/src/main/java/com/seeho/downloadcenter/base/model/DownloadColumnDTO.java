package com.seeho.downloadcenter.base.model;

import lombok.Data;

/**
 * Column definition submitted from the UI for dynamic exports.
 */
@Data
public class DownloadColumnDTO {

    private String field;
    private String header;
    private Boolean enable = Boolean.TRUE;
    private String dateFormat;
    private String numberFormat;
}

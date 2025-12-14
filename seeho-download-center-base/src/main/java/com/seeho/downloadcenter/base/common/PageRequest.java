package com.seeho.downloadcenter.base.common;

import lombok.Data;

/**
 * Basic pagination request payload.
 */
@Data
public class PageRequest {
    private Integer pageIndex = 1;
    private Integer pageSize = 10;
}

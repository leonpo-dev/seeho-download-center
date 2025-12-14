package com.seeho.downloadcenter.base.common;

import lombok.Data;

/**
 * 分页请求基类
 *
 * @author Leonpo
 * @since 2025-12-02
 */
@Data
public class PageRequest {
    private Integer pageIndex = 1;
    private Integer pageSize = 10;
}


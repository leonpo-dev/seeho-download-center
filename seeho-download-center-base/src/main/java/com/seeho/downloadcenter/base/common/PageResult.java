package com.seeho.downloadcenter.base.common;

import lombok.Data;

import java.util.List;

/**
 * Immutable pagination result wrapper.
 */
@Data
public class PageResult<T> {
    private List<T> records;
    private Long total;
    private Integer pageIndex;
    private Integer pageSize;

    public static <T> PageResult<T> of(List<T> records, Long total, Integer pageIndex, Integer pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setPageIndex(pageIndex);
        result.setPageSize(pageSize);
        return result;
    }

    public <R> PageResult<R> copyTo(Class<R> targetClass) {
        // Caller is responsible for copying records to the desired type.
        PageResult<R> result = new PageResult<>();
        result.setTotal(this.total);
        result.setPageIndex(this.pageIndex);
        result.setPageSize(this.pageSize);
        return result;
    }
}

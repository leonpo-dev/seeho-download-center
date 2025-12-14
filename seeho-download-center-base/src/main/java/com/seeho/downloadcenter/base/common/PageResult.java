package com.seeho.downloadcenter.base.common;

import lombok.Data;

import java.util.List;

/**
 * 分页结果类
 *
 * @author Leonpo
 * @since 2025-12-02
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

    /**
     * 转换为另一个类型的分页结果
     */
    public <R> PageResult<R> copyTo(Class<R> targetClass) {
        // 这里简化处理，实际使用时需要BeanUtil.copyList
        // 暂时返回空，调用者需要手动转换
        PageResult<R> result = new PageResult<>();
        result.setTotal(this.total);
        result.setPageIndex(this.pageIndex);
        result.setPageSize(this.pageSize);
        // records需要调用者手动转换
        return result;
    }
}


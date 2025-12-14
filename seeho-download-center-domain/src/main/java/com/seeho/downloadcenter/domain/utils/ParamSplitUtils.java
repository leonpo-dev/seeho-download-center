package com.seeho.downloadcenter.domain.utils;

import com.seeho.downloadcenter.base.common.PageRequest;
import org.springframework.util.Assert;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Leonpo
 * @since 2025-11-26
 */
@Slf4j
public class ParamSplitUtils<E extends PageRequest> {

    /**
     * 最小的时间分片大小，单位：秒
     * <br>注意：不能小于2
     */
    public final int MIN_PART_SIZE = 1;
    /**
     * 最大的时间分片大小(12小时)，单位：秒
     */
    public final int MAX_PART_SIZE = 43200;

    /**
     * 获取开始时间的回调函数
     */
    private Function<E, LocalDateTime> getStart;
    /**
     * 获取结束时间的回调函数
     */
    private Function<E, LocalDateTime> getEnd;
    /**
     * 设置开始时间的回调函数
     */
    private BiConsumer<E, LocalDateTime> setStart;
    /**
     * 设置结束时间的回调函数
     */
    private BiConsumer<E, LocalDateTime> setEnd;
    /**
     * 统计数据行数的回调函数
     */
    private Function<E, Long> count;
    /**
     * 每个分片在切分的时候，按照这个数量预估切分
     */
    private int paramRows = 5000;
    /**
     * 每个分片允许查询的数据行数，如果时间段内的数据超过这个数量，会报错
     */
    private int maxQueryRows = paramRows * 3;

    /**
     * 当前分片索引，从1开始
     */
    private int pageIndex = 1;


    /**
     * 私有构造方法
     */
    private ParamSplitUtils(Function<E, LocalDateTime> getStart,
                            Function<E, LocalDateTime> getEnd,
                            BiConsumer<E, LocalDateTime> setStart,
                            BiConsumer<E, LocalDateTime> setEnd,
                            Function<E, Long> count) {
        this.getStart = getStart;
        this.getEnd = getEnd;
        this.setStart = setStart;
        this.setEnd = setEnd;
        this.count = count;
    }

    /**
     * 创建基于 LocalDateTime 的分片工具
     *
     * @param getStart 获取开始时间的函数
     * @param getEnd   获取结束时间的函数
     * @param setStart 设置开始时间的函数
     * @param setEnd   设置结束时间的函数
     * @param count    统计数据行数的函数
     * @param <E>      参数类型
     * @return ParamSplitUtils 实例
     */
    public static <E extends PageRequest> ParamSplitUtils<E> forLocalDateTime(
            Function<E, LocalDateTime> getStart,
            Function<E, LocalDateTime> getEnd,
            BiConsumer<E, LocalDateTime> setStart,
            BiConsumer<E, LocalDateTime> setEnd,
            Function<E, Long> count) {
        return new ParamSplitUtils<>(getStart, getEnd, setStart, setEnd, count);
    }

    /**
     * 创建基于 LocalDate 的分片工具
     * <p>
     * LocalDate 会被转换为 LocalDateTime：
     * - 开始日期 → 当天 00:00:00
     * - 结束日期 → 当天 23:59:59
     * </p>
     *
     * @param getStartDate 获取开始日期的函数
     * @param getEndDate   获取结束日期的函数
     * @param setStartDate 设置开始日期的函数
     * @param setEndDate   设置结束日期的函数
     * @param count        统计数据行数的函数
     * @param <E>          参数类型
     * @return ParamSplitUtils 实例
     */
    public static <E extends PageRequest> ParamSplitUtils<E> forLocalDate(
            Function<E, LocalDate> getStartDate,
            Function<E, LocalDate> getEndDate,
            BiConsumer<E, LocalDate> setStartDate,
            BiConsumer<E, LocalDate> setEndDate,
            Function<E, Long> count) {

        // 将 LocalDate 的 getter 转换为 LocalDateTime 的 getter
        Function<E, LocalDateTime> getStart = e -> {
            LocalDate date = getStartDate.apply(e);
            return date == null ? null : date.atStartOfDay();
        };

        Function<E, LocalDateTime> getEnd = e -> {
            LocalDate date = getEndDate.apply(e);
            // 结束日期转为当天的 23:59:59
            return date == null ? null : date.atTime(23, 59, 59);
        };

        // 将 LocalDateTime 的 setter 转换为 LocalDate 的 setter
        BiConsumer<E, LocalDateTime> setStart = (e, dateTime) -> {
            if (dateTime != null) {
                setStartDate.accept(e, dateTime.toLocalDate());
            } else {
                setStartDate.accept(e, null);
            }
        };

        BiConsumer<E, LocalDateTime> setEnd = (e, dateTime) -> {
            if (dateTime != null) {
                setEndDate.accept(e, dateTime.toLocalDate());
            } else {
                setEndDate.accept(e, null);
            }
        };

        return new ParamSplitUtils<>(getStart, getEnd, setStart, setEnd, count);
    }

    /**
     * 二分法，把查询条件分片，返回所有分割后的查询条件
     * <p>
     * 通过递归二分切分时间范围，确保每段查询数据量 ≤ paramRows，
     * 若时间粒度已达 MIN_PART_SIZE 仍超 maxQueryRows，则视为热点数据并抛异常。
     * </p>
     *
     * @param e 原始查询条件（包含时间范围及其他筛选条件）
     * @return 分片后的查询条件列表，按时间从早到晚排序
     * @throws RuntimeException 当热点时间段数据量超过 maxQueryRows 时
     */
    public List<E> split(E e) {
        List<E> result = new LinkedList<>();
        doSplit(e, result);
        return result;
    }

    /**
     * 递归切分逻辑
     *
     * @param segment 当前段查询条件
     * @param result  结果集合
     */
    private void doSplit(E segment, List<E> result) {
        LocalDateTime start = getStart.apply(segment);
        LocalDateTime end = getEnd.apply(segment);

        // 计算当前段数据量
        Long count = this.count.apply(segment);

        // 若 count == 0，直接跳过（节省下游查询）
        if (count == null || count == 0) {
            log.debug("[ParamSplit] Segment has no data, skip. start={}, end={}", start, end);
            return;
        }

        // 若 count <= paramRows，直接收录
        if (count <= paramRows) {
            log.debug("[ParamSplit] Segment within limit, add to result. start={}, end={}, count={}", start, end, count);
            segment.setPageSize(paramRows);
            segment.setPageIndex(pageIndex);
            result.add(segment);
            return;
        }

        // count > paramRows，需要切分
        long durationSeconds = ChronoUnit.SECONDS.between(start, end);

        // 若时间距离 <= MIN_PART_SIZE，判断是否热点数据
        if (durationSeconds <= MIN_PART_SIZE) {
            Assert.isTrue(count <= maxQueryRows,
                String.format("[热点数据] 时间段内数据量过大，count=%d > maxQueryRows=%d, start=%s, end=%s",
                    count, maxQueryRows, start, end));
            // 允许略超 paramRows，但不超 maxQueryRows
            log.warn("[ParamSplit] Minimal segment exceeds paramRows but within maxQueryRows. start={}, end={}, count={}",
                    start, end, count);
            segment.setPageSize(paramRows);
            segment.setPageIndex(pageIndex);
            result.add(segment);
            return;
        }

        // 二分切分
        long halfDuration = durationSeconds / 2;
        LocalDateTime mid = start.plusSeconds(halfDuration);

        log.debug("[ParamSplit] Splitting segment. start={}, mid={}, end={}, count={}", start, mid, end, count);

        // 左段 [start, mid]
        E leftSegment = BeanUtil.copy(segment, (Class<E>) segment.getClass());
        setStart.accept(leftSegment, start);
        setEnd.accept(leftSegment, mid);
        doSplit(leftSegment, result);

        // 右段 [mid+1秒, end]（避免闭区间重叠）
        E rightSegment = BeanUtil.copy(segment, (Class<E>) segment.getClass());
        setStart.accept(rightSegment, mid.plusSeconds(1));
        setEnd.accept(rightSegment, end);
        doSplit(rightSegment, result);
    }

}

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
 * Utility that splits time-ranged queries into manageable segments.
 */
@Slf4j
public class ParamSplitUtils<E extends PageRequest> {

    public final int MIN_PART_SIZE = 1;
    public final int MAX_PART_SIZE = 43200;

    private Function<E, LocalDateTime> getStart;
    private Function<E, LocalDateTime> getEnd;
    private BiConsumer<E, LocalDateTime> setStart;
    private BiConsumer<E, LocalDateTime> setEnd;
    private Function<E, Long> count;
    private int paramRows = 5000;
    private int maxQueryRows = paramRows * 3;

    private int pageIndex = 1;


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

    public static <E extends PageRequest> ParamSplitUtils<E> forLocalDateTime(
            Function<E, LocalDateTime> getStart,
            Function<E, LocalDateTime> getEnd,
            BiConsumer<E, LocalDateTime> setStart,
            BiConsumer<E, LocalDateTime> setEnd,
            Function<E, Long> count) {
        return new ParamSplitUtils<>(getStart, getEnd, setStart, setEnd, count);
    }

    public static <E extends PageRequest> ParamSplitUtils<E> forLocalDate(
            Function<E, LocalDate> getStartDate,
            Function<E, LocalDate> getEndDate,
            BiConsumer<E, LocalDate> setStartDate,
            BiConsumer<E, LocalDate> setEndDate,
            Function<E, Long> count) {

        Function<E, LocalDateTime> getStart = e -> {
            LocalDate date = getStartDate.apply(e);
            return date == null ? null : date.atStartOfDay();
        };

        Function<E, LocalDateTime> getEnd = e -> {
            LocalDate date = getEndDate.apply(e);
            return date == null ? null : date.atTime(23, 59, 59);
        };

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

    public List<E> split(E e) {
        List<E> result = new LinkedList<>();
        doSplit(e, result);
        return result;
    }

    private void doSplit(E segment, List<E> result) {
        LocalDateTime start = getStart.apply(segment);
        LocalDateTime end = getEnd.apply(segment);

        Long count = this.count.apply(segment);

        if (count == null || count == 0) {
            log.debug("[ParamSplit] Segment has no data, skip. start={}, end={}", start, end);
            return;
        }

        if (count <= paramRows) {
            log.debug("[ParamSplit] Segment within limit, add to result. start={}, end={}, count={}", start, end, count);
            segment.setPageSize(paramRows);
            segment.setPageIndex(pageIndex);
            result.add(segment);
            return;
        }

        long durationSeconds = ChronoUnit.SECONDS.between(start, end);

        if (durationSeconds <= MIN_PART_SIZE) {
            Assert.isTrue(count <= maxQueryRows,
                String.format("[HotSpot] Segment contains too many rows, count=%d > maxQueryRows=%d, start=%s, end=%s",
                    count, maxQueryRows, start, end));
            // Allow segments slightly above paramRows as long as they stay under maxQueryRows.
            log.warn("[ParamSplit] Minimal segment exceeds paramRows but within maxQueryRows. start={}, end={}, count={}",
                    start, end, count);
            segment.setPageSize(paramRows);
            segment.setPageIndex(pageIndex);
            result.add(segment);
            return;
        }

        long halfDuration = durationSeconds / 2;
        LocalDateTime mid = start.plusSeconds(halfDuration);

        log.debug("[ParamSplit] Splitting segment. start={}, mid={}, end={}, count={}", start, mid, end, count);

        E leftSegment = BeanUtil.copy(segment, (Class<E>) segment.getClass());
        setStart.accept(leftSegment, start);
        setEnd.accept(leftSegment, mid);
        doSplit(leftSegment, result);

        E rightSegment = BeanUtil.copy(segment, (Class<E>) segment.getClass());
        setStart.accept(rightSegment, mid.plusSeconds(1));
        setEnd.accept(rightSegment, end);
        doSplit(rightSegment, result);
    }

}

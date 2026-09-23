package com.owen.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.MDC;
import org.slf4j.Marker;

import java.util.HashMap;
import java.util.Map;

/**
 * 在 LoggingEvent 创建前把业务线程 ID 写入 MDC。
 * AsyncAppender 出队时已经是别的线程，不能再 {@code Thread.currentThread()}。
 * Nacos 等组件可能把 MDC 设成不可变拷贝，{@code put} 会失败，这时改写一份可变 map。
 */
public class ThreadMdcTurboFilter extends TurboFilter {

    public static final String THREAD_ID = "thread_id";

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        String threadId = String.valueOf(Thread.currentThread().threadId());
        if (threadId.equals(MDC.get(THREAD_ID))) {
            return FilterReply.NEUTRAL;
        }
        try {
            MDC.put(THREAD_ID, threadId);
        } catch (RuntimeException ignored) {
            Map<String, String> copy = MDC.getCopyOfContextMap();
            Map<String, String> next = copy == null ? new HashMap<>() : new HashMap<>(copy);
            next.put(THREAD_ID, threadId);
            MDC.setContextMap(next);
        }
        return FilterReply.NEUTRAL;
    }

}

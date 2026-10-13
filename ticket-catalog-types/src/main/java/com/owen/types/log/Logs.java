package com.owen.types.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;

import java.util.Map;

/**
 * 结构化日志。
 * <p>
 * title：日志标题，写入 message。按业务动作起名，例如「上海大剧院-可售场次」，搜索时先按标题定位。
 * content：这条日志的全部信息，写入 content。报文、旧值新值、发生了什么，都放这里。读这一条就能看完，不依赖 tags。
 * tags：可空。只放能拿来筛选、定位到这一条日志的键，每个键单独成字段。例如 programId、scheduleId、supplierProjectId。
 * 路径、响应原文、供应商名称、任务名、描述文字都不是筛选条件，不要放进来。没有可筛的键时不要传 tags。
 */
public final class Logs {

    private Logs() {
    }

    public static void info(String title, String content) {
        write(title, content, null, null, Level.INFO);
    }

    public static void info(String title, String content, Map<String, ?> tags) {
        write(title, content, tags, null, Level.INFO);
    }

    public static void warn(String title, String content) {
        write(title, content, null, null, Level.WARN);
    }

    public static void warn(String title, String content, Throwable cause) {
        write(title, content, null, cause, Level.WARN);
    }

    public static void warn(String title, String content, Map<String, ?> tags) {
        write(title, content, tags, null, Level.WARN);
    }

    public static void warn(String title, String content, Map<String, ?> tags, Throwable cause) {
        write(title, content, tags, cause, Level.WARN);
    }

    public static void error(String title, String content) {
        write(title, content, null, null, Level.ERROR);
    }

    public static void error(String title, String content, Throwable cause) {
        write(title, content, null, cause, Level.ERROR);
    }

    public static void error(String title, String content, Map<String, ?> tags) {
        write(title, content, tags, null, Level.ERROR);
    }

    public static void error(String title, String content, Map<String, ?> tags, Throwable cause) {
        write(title, content, tags, cause, Level.ERROR);
    }

    private static void write(String title, String content, Map<String, ?> tags, Throwable cause, Level level) {
        Logger logger = LoggerFactory.getLogger(caller());
        LoggingEventBuilder event = switch (level) {
            case INFO -> logger.atInfo();
            case WARN -> logger.atWarn();
            case ERROR -> logger.atError();
        };
        if (content != null) {
            event = event.addKeyValue("content", content);
        }
        if (tags != null) {
            for (Map.Entry<String, ?> tag : tags.entrySet()) {
                if (tag.getValue() != null) {
                    event = event.addKeyValue(tag.getKey(), tag.getValue());
                }
            }
        }
        if (cause != null) {
            event = event.setCause(cause);
        }
        event.log(title);
    }

    private static String caller() {
        return StackWalker.getInstance()
                .walk(frames -> frames.skip(2).findFirst().map(StackWalker.StackFrame::getClassName))
                .orElse(Logs.class.getName());
    }

    private enum Level {
        INFO, WARN, ERROR
    }

}

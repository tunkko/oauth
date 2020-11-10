package org.tunkko;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 日志工具类
 *
 * @author tunkko
 * @version 1.0
 * @date 2020/7/15
 */
public class Logger {

    private static final Log log = LogFactory.getLog(Logger.class);

    public static void info(String msg, Object... args) {
        log.info(String.format("\033[1;1m" + msg + "\033[0m", args));
    }

    public static void warn(String msg, Object... args) {
        log.warn(String.format("\033[1;35m " + msg + " \033[0m", args));
    }

    public static void error(String msg, Object... args) {
        log.error(String.format("\033[1;31m " + msg + " \033[0m", args));
    }
}

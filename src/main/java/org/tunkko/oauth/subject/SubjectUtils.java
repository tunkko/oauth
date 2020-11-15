package org.tunkko.oauth.subject;

import java.util.Map;

/**
 * 主体调用工具类
 *
 * @author tunkko
 */
public class SubjectUtils {

    private static final ThreadLocal<Subject> LOCAL = new ThreadLocal<>();

    public static void set(Subject subject) {
        LOCAL.set(subject);
    }

    public static Subject get() {
        return LOCAL.get();
    }

    public static Object getUserId() {
        return get().getUserId();
    }

    public static Map<String, Object> getClaims() {
        return get().getClaims();
    }

    public static void remove() {
        LOCAL.remove();
    }
}

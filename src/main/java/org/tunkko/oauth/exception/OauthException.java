package org.tunkko.oauth.exception;

/**
 * 安全异常
 *
 * @author tunkko
 * @version 1.0
 * @since 2020/7/15
 */
public class OauthException extends RuntimeException {

    public OauthException(String msg) {
        super(msg);
    }
}

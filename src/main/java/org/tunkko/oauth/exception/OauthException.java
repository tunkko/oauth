package org.tunkko.oauth.exception;

/**
 * 安全异常
 *
 * @author tunkko
 */
public class OauthException extends RuntimeException {

    public OauthException(String msg) {
        super(msg);
    }
}

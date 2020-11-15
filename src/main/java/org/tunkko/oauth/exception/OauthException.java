package org.tunkko.oauth.exception;

/**
 * 认证异常
 *
 * @author tunkko
 */
public class OauthException extends RuntimeException {

    public OauthException(String msg) {
        super(msg);
    }
}

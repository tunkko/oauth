package org.tunkko.oauth.exception;

/**
 * 拒绝访问
 *
 * @author tunkko
 * @version 1.0
 * @since 2020/7/15
 */
public class ForbiddenException extends OauthException {

    public ForbiddenException(String msg) {
        super(msg);
    }
}

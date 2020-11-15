package org.tunkko.oauth.exception;

/**
 * 拒绝访问
 *
 * @author tunkko
 */
public class ForbiddenException extends OauthException {

    public ForbiddenException(String msg) {
        super(msg);
    }
}

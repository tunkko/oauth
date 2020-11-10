package org.tunkko.oauth.exception;

/**
 * 账号锁定
 *
 * @author tunkko
 * @version 1.0
 * @date 2020/7/15
 */
public class AccountLockedException extends OauthException {

    /**
     * 次数
     */
    private Integer frequency;

    /**
     * 时长
     */
    private Long duration;

    public AccountLockedException(String msg, Integer frequency, Long duration) {
        super(msg);
        this.frequency = frequency;
        this.duration = duration;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public Long getDuration() {
        return duration;
    }
}

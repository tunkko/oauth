package org.tunkko.oauth.token.store;

import org.tunkko.oauth.exception.AccountLockedException;
import org.tunkko.oauth.exception.OauthException;
import org.tunkko.oauth.token.Token;

import java.util.List;

/**
 * 令牌存储
 *
 * @author tunkko
 * @version 1.0
 * @date 2020/7/15
 */
public interface TokenStore {

    String OAUTH_KEY = "oauth_key";
    String OAUTH_LOCK = "oauth_lock";
    String OAUTH_TOKEN = "oauth_token";
    String OAUTH_PERMIT = "oauth_permit";

    /**
     * 密钥
     *
     * @return String
     */
    String getKey();

    /**
     * 存储锁定状态
     *
     * @param userId    用户ID
     * @param frequency 次数
     * @param duration  时长
     * @throws AccountLockedException 异常
     */
    void storeLockStatus(Object userId, Integer frequency, Long duration) throws AccountLockedException;

    /**
     * 删除锁定状态
     *
     * @param userId 用户ID
     */
    void delLockStatus(Object userId);

    /**
     * 存储令牌
     *
     * @param token Token
     * @return String
     * @throws OauthException 异常
     */
    String storeToken(Token token) throws OauthException;

    /**
     * 获取令牌
     *
     * @param accessToken 访问令牌
     * @return Boolean
     */
    Token findToken(String accessToken);

    /**
     * 删除token
     *
     * @param userId 用户ID
     */
    void delAllToken(Object userId);

    /**
     * 删除token
     *
     * @param accessToken 访问令牌
     */
    void delToken(String accessToken);

    /**
     * 存储许可
     *
     * @param userId  用户ID
     * @param permits 许可
     */
    void storePermit(Object userId, List<String> permits);

    /**
     * 获取许可
     *
     * @param userId 用户ID
     * @return List
     */
    List<String> getPermit(Object userId);
}

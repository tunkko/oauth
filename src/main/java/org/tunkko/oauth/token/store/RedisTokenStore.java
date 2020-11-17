package org.tunkko.oauth.token.store;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.tunkko.oauth.utils.Logger;
import org.tunkko.oauth.exception.AccountLockedException;
import org.tunkko.oauth.exception.OauthException;
import org.tunkko.oauth.token.Token;
import org.tunkko.oauth.utils.Crypto;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * redis令牌存储
 *
 * @author tunkko
 */
public class RedisTokenStore implements TokenStore {

    private final StringRedisTemplate redisTemplate;

    public RedisTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        Logger.info("------------------------------------------------------------------------");
        Logger.info("使用令牌存储方式: %s", redisTemplate.getClass().getSimpleName());
        Logger.info("------------------------------------------------------------------------");
    }

    @Override
    public String getKey() {
        String key = redisTemplate.opsForValue().get(OAUTH_KEY);
        if (StringUtils.isBlank(key)) {
            key = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(OAUTH_KEY, key);
        }
        return key;
    }

    @Override
    public void storeLockStatus(Object userId, Integer frequency, Long duration) throws AccountLockedException {
        String key = parse(OAUTH_LOCK, userId);

        Integer lockNum = 0;
        String value = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(value)) {
            lockNum = Integer.parseInt(value);
            Long expire = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
            if (lockNum >= frequency && expire != null) {
                throw new AccountLockedException("账号已锁定", lockNum, expire);
            }
            lockNum++;
        }
        // 重新存储锁定状态
        redisTemplate.opsForValue().set(key, Integer.toString(lockNum), duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public void delLockStatus(Object userId) {
        String key = parse(OAUTH_LOCK, userId);
        redisTemplate.delete(key);
    }

    @Override
    public String storeToken(Token token) throws OauthException {
        Object userId = token.getUserId();
        String accessToken = Crypto.encrypt(token.toJson(), getKey());
        if (StringUtils.isBlank(accessToken)) {
            throw new OauthException("令牌存储失败");
        }
        // 删除锁定状态
        delLockStatus(userId);

        String key = parse(OAUTH_TOKEN, userId, token.getCreateTime().getTime());
        String keys = parse(OAUTH_TOKEN, userId, "*");

        // 删除原有令牌
        Set<String> set = redisTemplate.keys(keys);
        Integer maxToken = token.getMaxToken(), size = set == null ? 0 : set.size();
        if (maxToken > 0 && size >= maxToken) {
            List<String> list = new ArrayList<>(set);
            Collections.sort(list, new Comparator<String>() {

                @Override
                public int compare(String o1, String o2) {
                    String time1 = o1.substring(o1.lastIndexOf(":") + 1);
                    String time2 = o2.substring(o2.lastIndexOf(":") + 1);
                    return (int) (Long.valueOf(time1) - Long.valueOf(time2));
                }
            });
            redisTemplate.delete(list.subList(0, size - maxToken + 1));
        }

        // 存储新令牌
        long validDuration = token.getValidDuration();
        if (validDuration > 0) {
            redisTemplate.opsForValue().set(key, accessToken, validDuration, TimeUnit.MILLISECONDS);
        } else {
            redisTemplate.opsForValue().set(key, accessToken);
        }

        // 存储许可
        storePermit(userId, token.getPermit());
        return accessToken;
    }

    @Override
    public Token findToken(String accessToken) {
        if (StringUtils.isNotBlank(accessToken)) {
            Token token = JSON.parseObject(Crypto.decrypt(accessToken, getKey()), Token.class);
            if (token != null) {
                Object userId = token.getUserId();
                String key = parse(OAUTH_TOKEN, userId, token.getCreateTime().getTime());
                if (StringUtils.equals(redisTemplate.opsForValue().get(key), accessToken)) {
                    token.setPermit(getPermit(userId));
                    return token;
                }
            }
        }
        return null;
    }

    @Override
    public void delAllToken(Object userId) {
        String tokenKeys = parse(OAUTH_TOKEN, userId, "*");
        String permitKey = parse(OAUTH_PERMIT, userId);

        // 删除锁定状态
        delLockStatus(userId);
        // 删除令牌
        redisTemplate.delete(redisTemplate.keys(tokenKeys));
        // 删除许可
        redisTemplate.delete(permitKey);
    }

    @Override
    public void delToken(String accessToken) {
        // 获取token
        Token token = findToken(accessToken);
        if (token != null) {
            Object userId = token.getUserId();
            String key = parse(OAUTH_TOKEN, userId, token.getCreateTime().getTime());
            String keys = parse(OAUTH_TOKEN, userId, "*");

            // 删除令牌
            redisTemplate.delete(key);

            // 用户没有其他令牌，删除用户所有信息
            if (CollectionUtils.isEmpty(redisTemplate.keys(keys))) {
                delAllToken(userId);
            }
        }
    }

    @Override
    public void storePermit(Object userId, List<String> permits) {
        String key = parse(OAUTH_PERMIT, userId);
        // 删除原数据
        redisTemplate.delete(key);
        if (CollectionUtils.isNotEmpty(permits)) {
            // 存储新数据
            redisTemplate.opsForList().rightPushAll(key, permits);
        }
    }

    @Override
    public List<String> getPermit(Object userId) {
        String key = parse(OAUTH_PERMIT, userId);
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    private String parse(String str, Object... args) {
        StringBuilder builder = new StringBuilder(str);
        for (Object arg : args) {
            builder.append(":").append(arg);
        }
        return builder.toString();
    }
}

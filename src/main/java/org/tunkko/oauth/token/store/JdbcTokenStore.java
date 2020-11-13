package org.tunkko.oauth.token.store;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.tunkko.Logger;
import org.tunkko.oauth.exception.AccountLockedException;
import org.tunkko.oauth.exception.OauthException;
import org.tunkko.oauth.token.Token;
import org.tunkko.tools.codec.Crypto;

import java.util.*;

/**
 * jdbc令牌存储
 *
 * @author tunkko
 * @version 1.0
 * @date 2020/7/15
 */
public class JdbcTokenStore implements TokenStore {

    private static final String C_T = "create table if not exists ";
    private static final String CHARSET = " engine=InnoDB default charset=utf8";

    private static final String C_T_KEY = String.format(
            "%s %s (value varchar(36), primary key (value) using btree)" + CHARSET,
            C_T, OAUTH_KEY);
    private static final String Q_KEY = String.format("select value from %s", OAUTH_KEY);
    private static final String I_KEY = String.format("insert into %s (value) values (?)", OAUTH_KEY);

    private static final String C_T_LOCK = String.format(
            "%s %s (user_id varchar(36) not null, frequency int(3), expire_time bigint(20), primary key (user_id) using btree)" + CHARSET,
            C_T, OAUTH_LOCK);
    private static final String Q_LOCK = String.format("select frequency, expire_time from %s where user_id = ?", OAUTH_LOCK);
    private static final String I_LOCK = String.format("insert into %s (user_id, frequency, expire_time) values (?, ?, ?)", OAUTH_LOCK);
    private static final String D_LOCK = String.format("delete from %s where user_id = ?", OAUTH_LOCK);

    private static final String C_T_TOKEN = String.format(
            "%s %s (`key` varchar(36) not null, access_token text, primary key (`key`) using btree)" + CHARSET,
            C_T, OAUTH_TOKEN);
    private static final String Q_TOKENS = String.format("select `key` from %s where instr(`key`, ?)", OAUTH_TOKEN);
    private static final String Q_TOKEN = String.format("select access_token from %s where `key` = ?", OAUTH_TOKEN);
    private static final String I_TOKEN = String.format("insert into %s (`key`, access_token) values (?, ?)", OAUTH_TOKEN);
    private static final String D_TOKEN = String.format("delete from %s where `key` = ?", OAUTH_TOKEN);

    private static final String C_T_PERMIT = String.format(
            "%s %s (user_id varchar(36) not null, permit varchar(36), primary key (user_id) using btree)" + CHARSET,
            C_T, OAUTH_PERMIT);
    private static final String Q_PERMIT = String.format("select permit from %s where user_id = ?", OAUTH_PERMIT);
    private static final String I_PERMIT = String.format("insert into %s (user_id, permit) values (?, ?)", OAUTH_PERMIT);
    private static final String D_PERMIT = String.format("delete from %s where user_id = ?", OAUTH_PERMIT);

    private final JdbcTemplate jdbcTemplate;

    public JdbcTokenStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        jdbcTemplate.update(C_T_KEY);
        jdbcTemplate.update(C_T_LOCK);
        jdbcTemplate.update(C_T_TOKEN);
        jdbcTemplate.update(C_T_PERMIT);
        Logger.info("------------------------------------------------------------------------");
        Logger.info("使用令牌存储方式: %s", jdbcTemplate.getClass().getSimpleName());
        Logger.info("使用数据表: [%s, %s, %s, %s]", OAUTH_KEY, OAUTH_LOCK, OAUTH_TOKEN, OAUTH_PERMIT);
        Logger.info("------------------------------------------------------------------------");
    }

    @Override
    public String getKey() {
        String key;
        try {
            key = jdbcTemplate.queryForObject(Q_KEY, String.class);
            if (StringUtils.isBlank(key)) {
                key = UUID.randomUUID().toString();
                jdbcTemplate.update(I_KEY, key);
            }
        } catch (EmptyResultDataAccessException ignored) {
            key = UUID.randomUUID().toString();
            jdbcTemplate.update(I_KEY, key);
        }
        return key;
    }

    @Override
    public void storeLockStatus(Object userId, Integer frequency, Long duration) throws AccountLockedException {
        Integer lockNum = 0;
        long time = System.currentTimeMillis();
        try {
            Map<String, Object> query = jdbcTemplate.queryForMap(Q_LOCK, userId);
            lockNum = (Integer) query.get("frequency");
            Long expireTime = (Long) query.get("expire_time");
            if (lockNum >= frequency && expireTime != null && expireTime > time) {
                throw new AccountLockedException("账号已锁定", lockNum, expireTime - time);
            }
            lockNum++;
        } catch (EmptyResultDataAccessException ignored) {
        }
        // 重新存储锁定状态
        jdbcTemplate.update(D_LOCK, userId);
        jdbcTemplate.update(I_LOCK, userId, lockNum, duration);
    }

    @Override
    public void delLockStatus(Object userId) {
        jdbcTemplate.update(D_LOCK, userId);
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

        // 删除原有令牌
        try {
            List<String> keys = jdbcTemplate.queryForList(Q_TOKENS, String.class, userId);
            Integer maxToken = token.getMaxToken(), size = keys == null ? 0 : keys.size();
            if (maxToken > 0 && size >= maxToken) {
                List<String> list = new ArrayList<>(keys);
                Collections.sort(list, new Comparator<String>() {

                    @Override
                    public int compare(String o1, String o2) {
                        String time1 = o1.substring(o1.lastIndexOf(":") + 1);
                        String time2 = o2.substring(o2.lastIndexOf(":") + 1);
                        return (int) (Long.valueOf(time1) - Long.valueOf(time2));
                    }
                });
                for (String k : list.subList(0, size - maxToken + 1)) {
                    jdbcTemplate.update(D_TOKEN, k);
                }
            }
        } catch (EmptyResultDataAccessException ignored) {
        }

        // 存储新令牌
        jdbcTemplate.update(I_TOKEN, userId + ":" + token.getCreateTime().getTime(), accessToken);

        // 存储许可
        storePermit(userId, token.getPermit());
        return accessToken;
    }

    @Override
    public Token findToken(String accessToken) {
        if (StringUtils.isNotBlank(accessToken)) {
            Token token = JSON.parseObject(Crypto.decrypt(accessToken, getKey()), Token.class);
            boolean bool = token != null && (token.getExpireTime() == null || token.getExpireTime().compareTo(new Date()) > 0);
            if (bool) {
                try {
                    Object userId = token.getUserId();
                    String query = jdbcTemplate.queryForObject(Q_TOKEN, String.class, userId + ":" + token.getCreateTime().getTime());
                    if (StringUtils.equals(query, accessToken)) {
                        token.setPermit(getPermit(userId));
                        return token;
                    }
                } catch (EmptyResultDataAccessException ignored) {
                }
            }
        }
        return null;
    }

    @Override
    public void delAllToken(Object userId) {
        // 删除锁定状态
        delLockStatus(userId);
        // 删除令牌
        try {
            for (String key : jdbcTemplate.queryForList(Q_TOKENS, String.class, userId)) {
                jdbcTemplate.update(D_TOKEN, key);
            }
        } catch (EmptyResultDataAccessException ignored) {
        }
        // 删除许可
        jdbcTemplate.update(D_PERMIT, userId);
    }

    @Override
    public void delToken(String accessToken) {
        // 获取token
        Token token = findToken(accessToken);
        if (token != null) {
            Object userId = token.getUserId();

            // 删除令牌
            jdbcTemplate.update(D_TOKEN, userId + ":" + token.getCreateTime().getTime());

            // 用户没有其他令牌，删除用户所有信息
            if (CollectionUtils.isEmpty(jdbcTemplate.queryForList(Q_TOKENS, String.class, userId))) {
                delAllToken(userId);
            }
        }
    }

    @Override
    public void storePermit(Object userId, List<String> permits) {
        // 删除原数据
        jdbcTemplate.update(D_PERMIT, userId);
        if (CollectionUtils.isNotEmpty(permits)) {
            // 存储新数据
            for (String permit : permits) {
                jdbcTemplate.update(I_PERMIT, userId, permit);
            }
        }
    }

    @Override
    public List<String> getPermit(Object userId) {
        try {
            return jdbcTemplate.queryForList(Q_PERMIT, String.class, userId);
        } catch (EmptyResultDataAccessException ignored) {
        }
        return new ArrayList<>();
    }
}

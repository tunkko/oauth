package org.tunkko.oauth.token;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.io.Serializable;
import java.util.*;

/**
 * 令牌
 *
 * @author tunkko
 * @version 1.0
 * @date 2020/7/15
 */
public class Token implements Serializable {

    /**
     * 用户ID
     */
    private Object userId;

    /**
     * 私有声明
     */
    private Map<String, Object> claims;

    /**
     * 一个主体可同时在线数量
     */
    private Integer maxToken;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 有效时长
     */
    private Long validDuration;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 许可
     */
    private List<String> permits;

    public Token() {
    }

    public Token(Object userId, List<String> permits) {
        this(userId, 0L, permits);
    }

    public Token(Object userId, Long validDuration, List<String> permits) {
        this(userId, null, 1, validDuration, permits);
    }

    public Token(Object userId, Map<String, Object> claims, List<String> permits) {
        this(userId, claims, 1, 0L, permits);
    }

    public Token(Object userId, Map<String, Object> claims, Long validDuration, List<String> permits) {
        this(userId, claims, 1, validDuration, permits);
    }

    public Token(Object userId, Map<String, Object> claims, Integer maxToken, Long validDuration, List<String> permits) {
        this.userId = userId;
        this.claims = MapUtils.isEmpty(claims) ? new HashMap<String, Object>() : claims;
        this.maxToken = maxToken < 1 ? 1 : maxToken;
        this.createTime = new Date();
        this.validDuration = validDuration;
        this.expireTime = validDuration == 0 ? null : new Date(System.currentTimeMillis() + validDuration);
        this.permits = CollectionUtils.isEmpty(permits) ? new ArrayList<String>() : permits;
    }

    public Object getUserId() {
        return userId;
    }

    public void setUserId(Object userId) {
        this.userId = userId;
    }

    public Map<String, Object> getClaims() {
        return claims;
    }

    public void setClaims(Map<String, Object> claims) {
        this.claims = claims;
    }

    public Integer getMaxToken() {
        return maxToken;
    }

    public void setMaxToken(Integer maxToken) {
        this.maxToken = maxToken;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getValidDuration() {
        return validDuration;
    }

    public void setValidDuration(Long validDuration) {
        this.validDuration = validDuration;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public List<String> getPermits() {
        return permits;
    }

    public void setPermits(List<String> permits) {
        this.permits = permits;
    }

    public String toJson() {
        JSONObject json = JSON.parseObject(JSON.toJSONString(this));
        json.remove("permits");
        return json.toJSONString();
    }
}

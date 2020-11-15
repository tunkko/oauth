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
     * 许可证
     */
    private List<String> permit;

    public Token() {
    }

    public Token(Object userId, List<String> permit) {
        this(userId, 0L, permit);
    }

    public Token(Object userId, Long validDuration, List<String> permit) {
        this(userId, null, 1, validDuration, permit);
    }

    public Token(Object userId, Map<String, Object> claims, List<String> permit) {
        this(userId, claims, 1, 0L, permit);
    }

    public Token(Object userId, Map<String, Object> claims, Long validDuration, List<String> permit) {
        this(userId, claims, 1, validDuration, permit);
    }

    public Token(Object userId, Map<String, Object> claims, Integer maxToken, Long validDuration, List<String> permit) {
        this.userId = userId;
        this.claims = MapUtils.isEmpty(claims) ? new HashMap<String, Object>() : claims;
        this.maxToken = maxToken < 1 ? 1 : maxToken;
        this.createTime = new Date();
        this.validDuration = validDuration;
        this.expireTime = validDuration == 0 ? null : new Date(System.currentTimeMillis() + validDuration);
        this.permit = CollectionUtils.isEmpty(permit) ? new ArrayList<String>() : permit;
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

    public List<String> getPermit() {
        return permit;
    }

    public void setPermit(List<String> permit) {
        this.permit = permit;
    }

    public String toJson() {
        JSONObject json = JSON.parseObject(JSON.toJSONString(this));
        json.remove("permit");
        return json.toJSONString();
    }
}

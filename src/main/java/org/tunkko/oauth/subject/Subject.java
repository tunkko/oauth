package org.tunkko.oauth.subject;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 主体
 *
 * @author tunkko
 * @version 1.0
 * @date 2020/7/15
 */
public class Subject implements Serializable {

    /**
     * 用户ID
     */
    private Object userId;

    /**
     * 私有声明
     */
    private Map<String, Object> claims;

    /**
     * 请求IP
     */
    private String reqIp;

    /**
     * 请求接口
     */
    private String reqUri;

    /**
     * 请求方法(GET/POST)
     */
    private String reqMethod;

    /**
     * 请求参数
     */
    private String reqParams;

    /**
     * 用户标识
     */
    private String userAgent;

    /**
     * 开始时间
     */
    private Date startTime;

    public Subject(Object userId, Map<String, Object> claims, String reqIp, String reqUri, String reqMethod, String reqParams, String userAgent) {
        this.userId = userId;
        this.claims = claims;
        this.reqIp = reqIp;
        this.reqUri = reqUri;
        this.reqMethod = reqMethod;
        this.reqParams = reqParams;
        this.userAgent = userAgent;
        this.startTime = new Date();
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

    public String getReqIp() {
        return reqIp;
    }

    public void setReqIp(String reqIp) {
        this.reqIp = reqIp;
    }

    public String getReqUri() {
        return reqUri;
    }

    public void setReqUri(String reqUri) {
        this.reqUri = reqUri;
    }

    public String getReqMethod() {
        return reqMethod;
    }

    public void setReqMethod(String reqMethod) {
        this.reqMethod = reqMethod;
    }

    public String getReqParams() {
        return reqParams;
    }

    public void setReqParams(String reqParams) {
        this.reqParams = reqParams;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String toJson() {
        return JSON.toJSONString(this);
    }
}

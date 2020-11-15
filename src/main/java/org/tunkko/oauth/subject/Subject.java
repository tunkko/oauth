package org.tunkko.oauth.subject;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.tunkko.oauth.filter.OauthRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 主体
 *
 * @author tunkko
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

    public Subject(Object userId, Map<String, Object> claims) {
        this.userId = userId;
        this.claims = MapUtils.isEmpty(claims) ? new HashMap<String, Object>() : claims;
        this.startTime = new Date();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            this.reqIp = getIp(request);
            this.reqUri = request.getRequestURI();
            this.reqMethod = request.getMethod();
            this.reqParams = getParams(request);
            this.userAgent = request.getHeader("User-Agent");
        }
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

    private String getParams(HttpServletRequest request) {
        if (request.getRequestURI().contains("login")) {
            return "{}";
        }

        JSONObject params = new JSONObject();
        Map<String, String[]> parameters = new HashMap<>(request.getParameterMap());
        parameters.remove("token");
        for (String key : parameters.keySet()) {
            params.put(key, StringUtils.join(parameters.get(key), ","));
        }

        String contentType = request.getHeader("Content-type");
        if (contentType != null && !contentType.startsWith("multipart/form-data")) {
            String body = new OauthRequestWrapper(request).getBody();
            if (StringUtils.isNotBlank(body)) {
                params.putAll(JSON.parseObject(body));
            }
        }

        return params.toJSONString();
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("Cdn-Src-Ip");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                } catch (UnknownHostException ignored) {
                }
            }
        }
        return ip;
    }
}

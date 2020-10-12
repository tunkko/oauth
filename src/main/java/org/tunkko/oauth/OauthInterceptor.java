package org.tunkko.oauth;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.tunkko.Logger;
import org.tunkko.oauth.annotation.Permit;
import org.tunkko.oauth.exception.ForbiddenException;
import org.tunkko.oauth.exception.OauthException;
import org.tunkko.oauth.subject.Subject;
import org.tunkko.oauth.subject.SubjectUtils;
import org.tunkko.oauth.token.Token;
import org.tunkko.oauth.token.store.TokenStore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 认证拦截器
 *
 * @author tunkko
 * @version 1.0
 * @date 2020/7/15
 */
public class OauthInterceptor implements HandlerInterceptor {

    private TokenStore tokenStore;

    private String[] includePaths;

    private String[] excludePaths;

    public OauthInterceptor(TokenStore tokenStore, String[] includePaths, String[] excludePaths) {
        this.tokenStore = tokenStore;
        this.includePaths = includePaths;
        this.excludePaths = excludePaths;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Logger.info("------------------------------------------------------------------------");
        Boolean flag = checkPath(request.getRequestURI());
        checkTokenAndPermit(request, handler, flag);
        Logger.info("------------------------------------------------------------------------");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SubjectUtils.remove();
    }

    private Boolean checkPath(String uri) throws ForbiddenException {
        Logger.info("检查放行路径: %s - %s", Arrays.toString(excludePaths), uri);
        if (ArrayUtils.isNotEmpty(excludePaths)) {
            for (String path : excludePaths) {
                String pattern = path.replaceAll("\\*", ".*") + "$";
                if (Pattern.matches(pattern, uri)) {
                    return true;
                }
            }
        }
        Logger.info("检查访问路径: %s - %s", Arrays.toString(includePaths), uri);
        if (ArrayUtils.isNotEmpty(includePaths)) {
            for (String path : includePaths) {
                String pattern = path.replaceAll("\\*", ".*") + "$";
                if (Pattern.matches(pattern, uri)) {
                    return false;
                }
            }
        }
        throw new ForbiddenException("请求路径无效");
    }

    private void checkTokenAndPermit(HttpServletRequest request, Object handler, Boolean flag) throws OauthException {
        String accessToken = request.getHeader("token");
        if (StringUtils.isBlank(accessToken)) {
            accessToken = request.getParameter("token");
        }

        String ip = getIp(request);
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String params = getParams(request);
        String userAgent = request.getHeader("User-Agent");

        if (flag) {
            Subject subject = new Subject(null, null, ip, uri, method, params, userAgent);
            SubjectUtils.set(subject);
            Logger.info("获取Subject: %s", subject.toJson());
            return;
        } else {
            Logger.info("解析access_token: %s", accessToken);
            Token token = tokenStore.findToken(accessToken);
            if (token != null) {
                Object userId = token.getUserId();
                List<String> permits = tokenStore.getPermits(userId);

                // 检查许可
                checkPermit(handler, permits);

                // 主体
                Map<String, Object> claims = token.getClaims();

                Subject subject = new Subject(userId, claims, ip, uri, method, params, userAgent);
                SubjectUtils.set(subject);
                Logger.info("获取Subject: %s", subject.toJson());
                return;
            }
        }
        throw new OauthException("身份验证失败");
    }

    private void checkPermit(Object handler, List<String> permits) throws ForbiddenException {
        if (handler instanceof HandlerMethod) {
            Method method = ((HandlerMethod) handler).getMethod();
            if (method.isAnnotationPresent(Permit.class)) {
                String[] values = method.getAnnotation(Permit.class).value();
                if (CollectionUtils.containsAny(Arrays.asList(values), permits)) {
                    return;
                }
            } else {
                return;
            }
        }
        throw new ForbiddenException("无权访问");
    }

    private String getParams(HttpServletRequest request) {
        Map<String, String[]> params = new HashMap<>(request.getParameterMap());
        params.remove("token");
        return JSON.toJSONString(params);
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

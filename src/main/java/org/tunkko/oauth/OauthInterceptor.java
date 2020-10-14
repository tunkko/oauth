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

    public OauthInterceptor(TokenStore tokenStore, String[] includePaths) {
        this.tokenStore = tokenStore;
        this.includePaths = includePaths;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Logger.info("------------------------------------------------------------------------");
        checkPath(request.getRequestURI());
        checkTokenAndPermit(request, handler);
        Logger.info("------------------------------------------------------------------------");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SubjectUtils.remove();
    }

    private void checkPath(String uri) throws ForbiddenException {
        Logger.info("检查访问路径: %s - %s", Arrays.toString(includePaths), uri);
        if (ArrayUtils.isNotEmpty(includePaths)) {
            for (String path : includePaths) {
                String pattern = path.replaceAll("\\*", ".*") + "$";
                if (Pattern.matches(pattern, uri)) {
                    return;
                }
            }
        }
        throw new ForbiddenException("请求路径无效");
    }

    private void checkTokenAndPermit(HttpServletRequest request, Object handler) throws OauthException {
        String accessToken = request.getHeader("token");
        if (StringUtils.isBlank(accessToken)) {
            accessToken = request.getParameter("token");
        }
        Logger.info("解析access_token: %s", accessToken);
        Token token = tokenStore.findToken(accessToken);
        if (token == null) {
            throw new OauthException("身份验证失败");
        }

        Object userId = token.getUserId();
        List<String> permits = tokenStore.getPermits(userId);

        // 检查许可
        checkPermit(handler, permits);

        // 主体
        Map<String, Object> claims = token.getClaims();
        Subject subject = new Subject(userId, claims);
        SubjectUtils.set(subject);
        Logger.info("主体Subject: %s", subject.toJson());
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
}

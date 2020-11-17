package org.tunkko.oauth;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.tunkko.oauth.utils.Logger;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 拦截器
 *
 * @author tunkko
 */
public class OauthInterceptor implements HandlerInterceptor {

    private TokenStore tokenStore;

    private String[] includePaths;

    public OauthInterceptor(TokenStore tokenStore, String[] includePaths) {
        this.tokenStore = tokenStore;
        this.includePaths = includePaths;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
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

    private void checkPath(String uri) {
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

    private void checkTokenAndPermit(HttpServletRequest request, Object handler) {
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
        List<String> permit = tokenStore.getPermit(userId);

        // 检查许可
        checkPermit(handler, permit);

        // 主体
        Map<String, Object> claims = token.getClaims();
        Subject subject = new Subject(userId, claims);
        SubjectUtils.set(subject);
        Logger.info("主体Subject: %s", subject.toJson());
    }

    private void checkPermit(Object handler, List<String> permit) {
        if (handler instanceof HandlerMethod) {
            Method method = ((HandlerMethod) handler).getMethod();
            Permit annotation = method.getAnnotation(Permit.class);
            if (annotation == null) {
                Class<?> clazz = method.getDeclaringClass();
                annotation = clazz.getAnnotation(Permit.class);
                if (annotation == null) {
                    return;
                }
            }

            String[] values = annotation.value();
            if (ArrayUtils.isEmpty(values) || CollectionUtils.containsAny(permit, values)) {
                return;
            }
        }
        throw new ForbiddenException("无权访问");
    }
}

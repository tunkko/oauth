package org.tunkko.oauth.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 过滤器
 *
 * @author tunkko
 * @version 1.0
 * @date 2020/7/15
 */
public class OauthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String contentType = req.getHeader("Content-type");
        if (contentType != null && !contentType.startsWith("multipart/form-data")) {
            OauthRequestWrapper wrapper = new OauthRequestWrapper(req);
            chain.doFilter(wrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}

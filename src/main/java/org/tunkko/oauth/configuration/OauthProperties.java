package org.tunkko.oauth.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置参数
 *
 * @author tunkko
 */
@ConfigurationProperties(prefix = "oauth")
public class OauthProperties {

    /**
     * 令牌存储方式(redis|jdbc)
     */
    private String storeType = "redis";

    /**
     * 拦截路径
     */
    private String[] paths = new String[]{"/*"};

    /**
     * 可认证路径
     */
    private String[] includePaths;

    /**
     * 放行路径
     */
    private String[] excludePaths = {};

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public String[] getPaths() {
        return paths;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }

    public String[] getIncludePaths() {
        return includePaths;
    }

    public void setIncludePaths(String[] includePaths) {
        this.includePaths = includePaths;
    }

    public String[] getExcludePaths() {
        return excludePaths;
    }

    public void setExcludePaths(String[] excludePaths) {
        this.excludePaths = excludePaths;
    }
}

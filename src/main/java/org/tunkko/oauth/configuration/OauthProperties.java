package org.tunkko.oauth.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.tunkko.oauth.enums.Env;
import org.tunkko.oauth.enums.StoreType;

/**
 * 配置参数
 *
 * @author tunkko
 */
@ConfigurationProperties(prefix = "oauth")
public class OauthProperties {

    /**
     * 令牌存储方式
     */
    private StoreType storeType = StoreType.redis;

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

    /**
     * 应对环境
     */
    private Env env = Env.PRO;

    public StoreType getStoreType() {
        return storeType;
    }

    public void setStoreType(StoreType storeType) {
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

    public Env getEnv() {
        return env;
    }

    public void setEnv(Env env) {
        this.env = env;
    }
}

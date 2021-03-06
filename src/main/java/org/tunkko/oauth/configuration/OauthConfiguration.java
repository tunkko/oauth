package org.tunkko.oauth.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.tunkko.oauth.OauthInterceptor;
import org.tunkko.oauth.enums.StoreType;
import org.tunkko.oauth.filter.OauthFilter;
import org.tunkko.oauth.token.store.JdbcTokenStore;
import org.tunkko.oauth.token.store.RedisTokenStore;
import org.tunkko.oauth.token.store.TokenStore;

/**
 * 配置
 *
 * @author tunkko
 */
@Configuration
@ConditionalOnProperty(prefix = "oauth", name = "include-paths")
@EnableConfigurationProperties({OauthProperties.class})
public class OauthConfiguration implements WebMvcConfigurer {

    private final ApplicationContext context;
    private final OauthProperties properties;

    public OauthConfiguration(ApplicationContext context, OauthProperties properties) {
        this.context = context;
        this.properties = properties;
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseRegisteredSuffixPatternMatch(true);
    }

    @Bean
    public TokenStore tokenStore() {
        StoreType storeType = properties.getStoreType();
        if (storeType == StoreType.redis) {
            StringRedisTemplate redisTemplate = context.getBean(StringRedisTemplate.class);
            if (redisTemplate != null) {
                return new RedisTokenStore(redisTemplate);
            }
        }
        if (storeType == StoreType.jdbc) {
            JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
            if (jdbcTemplate != null) {
                return new JdbcTokenStore(jdbcTemplate);
            }
        }
        return null;
    }

    @Bean
    @SuppressWarnings("unchecked")
    public FilterRegistrationBean filter() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new OauthFilter());
        return registrationBean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] paths = properties.getPaths();
        String[] includePaths = properties.getIncludePaths();
        String[] excludePaths = properties.getExcludePaths();
        registry.addInterceptor(new OauthInterceptor(tokenStore(), includePaths, properties.getEnv()))
                .addPathPatterns(paths)
                .excludePathPatterns(excludePaths);
    }
}

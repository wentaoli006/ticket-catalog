package com.owen.infrastructure.config;

import com.owen.domain.adapter.IDynamicConfig;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import org.springframework.stereotype.Component;

/**
 * Apollo 实现动态配置端口。Namespace 细节留在基础设施层。
 */
@Component
public class ApolloDynamicConfig implements IDynamicConfig {

    @ApolloConfig("application")
    private Config applicationConfig;

    @Override
    public String get(String key, String defaultValue) {
        return applicationConfig.getProperty(key, defaultValue);
    }

    @Override
    public Integer getInt(String key, Integer defaultValue) {
        return applicationConfig.getIntProperty(key, defaultValue);
    }

    @Override
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return applicationConfig.getBooleanProperty(key, defaultValue);
    }

}

package com.owen.config;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.owen.types.log.Logs;
import org.springframework.context.annotation.Configuration;

/**
 * Apollo 变更监听。配置被 Portal 发布后会推到本进程 Environment。
 * 线程池、Redisson 这类创建后不会自己变的 Bean，需要在这里按需重建。
 */
@Configuration
public class ApolloConfig {

    @ApolloConfigChangeListener
    public void onChange(ConfigChangeEvent changeEvent) {
        changeEvent.changedKeys().forEach(key ->
                Logs.info("apollo配置变更",
                        key + " " + changeEvent.getChange(key).getOldValue() + " -> " + changeEvent.getChange(key).getNewValue()));
    }

}

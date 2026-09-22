package com.owen.domain.xxx.service;

import com.owen.domain.adapter.IDynamicConfig;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 领域服务通过配置端口取值，不直接依赖 Apollo。
 */
@Service
public class XxxService {

    @Resource
    private IDynamicConfig dynamicConfig;

    public String queryWelcome() {
        return dynamicConfig.get("demo.welcome", "local-default");
    }

}

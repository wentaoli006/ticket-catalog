package com.owen.domain.adapter;

/**
 * 动态配置端口。领域只认业务 key，不认 Apollo / Namespace。
 */
public interface IDynamicConfig {

    String get(String key, String defaultValue);

    Integer getInt(String key, Integer defaultValue);

    Boolean getBoolean(String key, Boolean defaultValue);

}

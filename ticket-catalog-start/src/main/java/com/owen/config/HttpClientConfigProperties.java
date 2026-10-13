package com.owen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 出站 HTTP 超时和连接池。供应商地址不放这里，由各防腐客户端自己读取。
 */
@Data
@ConfigurationProperties(prefix = "http.client", ignoreInvalidFields = true)
public class HttpClientConfigProperties {

    /** 建立连接超时，毫秒 */
    private int connectTimeout = 3000;

    /** 读取响应超时，毫秒 */
    private int readTimeout = 5000;

    /** 连接池最大连接数 */
    private int maxConnections = 50;

    /** 等待连接池分配连接的超时，毫秒 */
    private int connectionRequestTimeout = 1000;

}

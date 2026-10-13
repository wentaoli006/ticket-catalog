package com.owen.config;

import com.owen.infrastructure.http.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * 装配带超时的出站 HTTP 客户端。业务代码不直接使用 RestClient。
 */
@Configuration
@EnableConfigurationProperties(HttpClientConfigProperties.class)
public class HttpClientConfig {

    @Bean
    public HttpClient httpClient(HttpClientConfigProperties properties) {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(properties.getMaxConnections());
        connectionManager.setDefaultMaxPerRoute(properties.getMaxConnections());
        connectionManager.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(properties.getConnectTimeout()))
                .build());

        org.apache.hc.client5.http.classic.HttpClient apacheClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(apacheClient);
        factory.setConnectionRequestTimeout(properties.getConnectionRequestTimeout());
        factory.setReadTimeout(properties.getReadTimeout());

        return new HttpClient(RestClient.builder().requestFactory(factory).build());
    }

}

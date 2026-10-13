package com.owen.infrastructure.http;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 带超时的出站 HTTP。超时和连接池在组合根装配，这里只发请求、拿报文。
 */
public class HttpClient {

    private final RestClient restClient;

    public HttpClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public <T> T get(String url, Class<T> bodyType) {
        return get(url, Map.of(), bodyType);
    }

    public <T> T get(String url, Map<String, ?> query, Class<T> bodyType) {
        return exchange(restClient.get().uri(uri(url, query)), bodyType);
    }

    public <T> T get(String url, ParameterizedTypeReference<T> bodyType) {
        return get(url, Map.of(), bodyType);
    }

    public <T> T get(String url, Map<String, ?> query, ParameterizedTypeReference<T> bodyType) {
        return exchange(restClient.get().uri(uri(url, query)), bodyType);
    }

    public <T> T post(String url, Object body, Class<T> bodyType) {
        return exchange(restClient.post().uri(uri(url)).contentType(MediaType.APPLICATION_JSON).body(body), bodyType);
    }

    public <T> T post(String url, Object body, ParameterizedTypeReference<T> bodyType) {
        return exchange(restClient.post().uri(uri(url)).contentType(MediaType.APPLICATION_JSON).body(body), bodyType);
    }

    private <T> T exchange(RestClient.RequestHeadersSpec<?> spec, Class<T> bodyType) {
        try {
            return spec.retrieve().body(bodyType);
        } catch (RestClientException exception) {
            throw new HttpRequestException("HTTP 请求失败", exception);
        }
    }

    private <T> T exchange(RestClient.RequestHeadersSpec<?> spec, ParameterizedTypeReference<T> bodyType) {
        try {
            return spec.retrieve().body(bodyType);
        } catch (RestClientException exception) {
            throw new HttpRequestException("HTTP 请求失败", exception);
        }
    }

    private static URI uri(String url) {
        return uri(url, Map.of());
    }

    private static URI uri(String url, Map<String, ?> query) {
        if (url == null || url.isBlank()) {
            throw new HttpRequestException("HTTP 地址不能为空");
        }
        String target = url;
        if (query != null && !query.isEmpty()) {
            StringBuilder builder = new StringBuilder(url);
            builder.append(url.contains("?") ? '&' : '?');
            boolean first = true;
            for (Map.Entry<String, ?> entry : query.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                if (!first) {
                    builder.append('&');
                }
                builder.append(encode(entry.getKey())).append('=').append(encode(String.valueOf(entry.getValue())));
                first = false;
            }
            target = builder.toString();
        }
        try {
            return URI.create(target);
        } catch (IllegalArgumentException exception) {
            throw new HttpRequestException("HTTP 地址不合法", exception);
        }
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}

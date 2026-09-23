package com.owen.infrastructure.adapter.port.shanghaigrandtheatre;

import com.owen.domain.adapter.IDynamicConfig;
import com.owen.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.List;

/**
 * 上海大剧院 HTTP 客户端。只取报文，不解释售卖规则。
 * 接口地址从 Apollo 的 application namespace 读取，每次请求重新取。
 */
@Slf4j
@Component
public class ShanghaiGrandTheatreClient {

    private static final String BASE_URL_KEY = "supplier.shanghai-grand-theatre.base-url";
    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:18080";
    private static final String PROGRAMS = "/theatre/partner/ticket/getAvailablePrograms.xhtml";
    private static final String SCHEDULES = "/theatre/partner/ticket/getAvailableSchedules.xhtml";
    private static final String PRICES = "/theatre/partner/ticket/getSchedulePrices.xhtml";

    private final RestClient restClient;
    private final IDynamicConfig dynamicConfig;

    public ShanghaiGrandTheatreClient(RestClient.Builder builder, IDynamicConfig dynamicConfig) {
        this.restClient = builder.build();
        this.dynamicConfig = dynamicConfig;
    }

    public List<ShanghaiGrandTheatreModels.Program> listPrograms() {
        return get(PROGRAMS, new ParameterizedTypeReference<>() {
        });
    }

    public List<ShanghaiGrandTheatreModels.Schedule> listSchedules(Long programId) {
        return get(SCHEDULES, "programId", programId, new ParameterizedTypeReference<>() {
        });
    }

    public List<ShanghaiGrandTheatreModels.Price> listPrices(Long scheduleId) {
        return get(PRICES, "scheduleId", scheduleId, new ParameterizedTypeReference<>() {
        });
    }

    private <T> List<T> get(String path, ParameterizedTypeReference<ShanghaiGrandTheatreModels.Response<T>> type) {
        try {
            return unwrap(path, restClient.get().uri(URI.create(endpoint(path))).retrieve().body(type));
        } catch (AppException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new AppException("SUPPLIER_HTTP_FAILED", "上海大剧院接口地址不合法", exception);
        } catch (RestClientException exception) {
            throw new AppException("SUPPLIER_HTTP_FAILED", "上海大剧院接口失败: " + path, exception);
        }
    }

    private <T> List<T> get(String path, String name, Long value,
                            ParameterizedTypeReference<ShanghaiGrandTheatreModels.Response<T>> type) {
        try {
            ShanghaiGrandTheatreModels.Response<T> response = restClient.get()
                    .uri(URI.create(endpoint(path) + "?" + name + "=" + value))
                    .retrieve()
                    .body(type);
            return unwrap(path, response);
        } catch (AppException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new AppException("SUPPLIER_HTTP_FAILED", "上海大剧院接口地址不合法", exception);
        } catch (RestClientException exception) {
            throw new AppException("SUPPLIER_HTTP_FAILED", "上海大剧院接口失败: " + path, exception);
        }
    }

    private String endpoint(String path) {
        String baseUrl = dynamicConfig.get(BASE_URL_KEY, DEFAULT_BASE_URL);
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = DEFAULT_BASE_URL;
        }
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + path;
    }

    private <T> List<T> unwrap(String path, ShanghaiGrandTheatreModels.Response<T> response) {
        if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
            throw new AppException("SUPPLIER_HTTP_FAILED", "上海大剧院接口失败: " + path);
        }
        List<T> data = response.getData() == null ? List.of() : response.getData();
        log.info("上海大剧院接口返回 path={} size={}", path, data.size());
        return data;
    }

}

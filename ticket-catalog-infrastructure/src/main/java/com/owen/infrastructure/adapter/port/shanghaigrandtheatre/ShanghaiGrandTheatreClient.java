package com.owen.infrastructure.adapter.port.shanghaigrandtheatre;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.owen.domain.adapter.IDynamicConfig;
import com.owen.infrastructure.http.HttpClient;
import com.owen.infrastructure.http.HttpRequestException;
import com.owen.types.exception.AppException;
import com.owen.types.log.Logs;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 上海大剧院 HTTP 客户端。只取报文，不解释售卖规则。
 * 接口地址从 Apollo 的 application namespace 读取，每次请求重新取。
 */
@Component
public class ShanghaiGrandTheatreClient {

    private static final String BASE_URL_KEY = "supplier.shanghai-grand-theatre.base-url";
    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:18080";
    private static final String PROGRAMS = "/theatre/partner/ticket/getAvailablePrograms.xhtml";
    private static final String SCHEDULES = "/theatre/partner/ticket/getAvailableSchedules.xhtml";
    private static final String PRICES = "/theatre/partner/ticket/getSchedulePrices.xhtml";
    private static final ObjectMapper JSON = new ObjectMapper();

    private final HttpClient httpClient;
    private final IDynamicConfig dynamicConfig;

    public ShanghaiGrandTheatreClient(HttpClient httpClient, IDynamicConfig dynamicConfig) {
        this.httpClient = httpClient;
        this.dynamicConfig = dynamicConfig;
    }

    public List<ShanghaiGrandTheatreModels.Program> listPrograms() {
        String body;
        try {
            body = httpClient.get(endpoint(PROGRAMS), String.class);
        } catch (HttpRequestException exception) {
            Logs.warn("上海大剧院-可售项目", PROGRAMS, exception);
            throw new AppException("SUPPLIER_HTTP_FAILED", "上海大剧院-可售项目失败", exception);
        }
        Logs.info("上海大剧院-可售项目", body);
        return unwrap("上海大剧院-可售项目", read(body, new ParameterizedTypeReference<>() {
        }));
    }

    public List<ShanghaiGrandTheatreModels.Schedule> listSchedules(Long programId) {
        String body;
        try {
            body = httpClient.get(endpoint(SCHEDULES), Map.of("programId", programId), String.class);
        } catch (HttpRequestException exception) {
            Logs.warn("上海大剧院-可售场次", SCHEDULES + " programId=" + programId, Map.of("programId", programId), exception);
            throw new AppException("SUPPLIER_HTTP_FAILED", "上海大剧院-可售场次失败", exception);
        }
        Logs.info("上海大剧院-可售场次", body, Map.of("programId", programId));
        return unwrap("上海大剧院-可售场次", read(body, new ParameterizedTypeReference<>() {
        }));
    }

    public List<ShanghaiGrandTheatreModels.Price> listPrices(Long scheduleId) {
        String body;
        try {
            body = httpClient.get(endpoint(PRICES), Map.of("scheduleId", scheduleId), String.class);
        } catch (HttpRequestException exception) {
            Logs.warn("上海大剧院-场次票价", PRICES + " scheduleId=" + scheduleId, Map.of("scheduleId", scheduleId), exception);
            throw new AppException("SUPPLIER_HTTP_FAILED", "上海大剧院-场次票价失败", exception);
        }
        Logs.info("上海大剧院-场次票价", body, Map.of("scheduleId", scheduleId));
        return unwrap("上海大剧院-场次票价", read(body, new ParameterizedTypeReference<>() {
        }));
    }

    private <T> ShanghaiGrandTheatreModels.Response<T> read(String body,
                                                             ParameterizedTypeReference<ShanghaiGrandTheatreModels.Response<T>> type) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return JSON.readValue(body, JSON.constructType(type.getType()));
        } catch (JsonProcessingException exception) {
            throw new AppException("SUPPLIER_HTTP_FAILED", "上海大剧院报文无法解析", exception);
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

    private <T> List<T> unwrap(String api, ShanghaiGrandTheatreModels.Response<T> response) {
        if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
            throw new AppException("SUPPLIER_HTTP_FAILED", api + "失败");
        }
        return response.getData() == null ? List.of() : response.getData();
    }

}

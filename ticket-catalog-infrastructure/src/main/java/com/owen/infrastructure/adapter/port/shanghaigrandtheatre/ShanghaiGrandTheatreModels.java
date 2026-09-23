package com.owen.infrastructure.adapter.port.shanghaigrandtheatre;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 上海大剧院开放接口报文。只在防腐层使用，不进入领域。
 */
public final class ShanghaiGrandTheatreModels {

    private ShanghaiGrandTheatreModels() {
    }

    @Data
    @NoArgsConstructor
    public static class Response<T> {
        private Boolean success;
        private List<T> data;
    }

    @Data
    @NoArgsConstructor
    public static class Program {
        private Long id;
        private String cnName;
        private String supportSeat;
        private String status;
        private String stadiumName;
        private String venueName;
        private String stadiumAddress;
    }

    @Data
    @NoArgsConstructor
    public static class Schedule {
        private Long id;
        private String cnName;
        private String playTime;
        private String playEndTime;
        private String opentime;
        private String closetime;
        private String status;
        private String display;
        private String checkcard;
        private Integer maxBuyLimit;
    }

    @Data
    @NoArgsConstructor
    public static class Price {
        private Long id;
        private Long ticketPriceId;
        private PriceInfo priceInfo;
    }

    @Data
    @NoArgsConstructor
    public static class PriceInfo {
        private Long id;
        private String description;
        private Integer unitnum;
        private BigDecimal price;
        private String cnName;
        private String remark;
    }

}

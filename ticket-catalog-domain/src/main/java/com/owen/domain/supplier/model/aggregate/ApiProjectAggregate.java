package com.owen.domain.supplier.model.aggregate;

import com.owen.domain.supplier.model.entity.ApiSessionEntity;
import com.owen.domain.supplier.model.valobj.RawProjectVO;
import com.owen.domain.supplier.model.valobj.RawSessionVO;
import com.owen.types.enums.SaleStatusEnum;
import com.owen.types.enums.StockModeEnum;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 原始项目聚合根。一次静态拉取内，项目、场次、票档的一致性由本对象保证。
 */
public class ApiProjectAggregate {

    private Long id;
    private final Long supplierId;
    private final String supplierProjectId;
    private Long poiId;
    private String name;
    private String posterUrl;
    private String ticketNotice;
    private SaleStatusEnum saleStatus;
    private boolean realName;
    private boolean seatSelection;
    private ZoneId timezone;
    private LocalDateTime earliestSessionStartTime;
    private LocalDateTime latestSessionStartTime;
    private final StockModeEnum stockMode;
    private final Map<String, ApiSessionEntity> sessions = new LinkedHashMap<>();

    private ApiProjectAggregate(Long id, Long supplierId, String supplierProjectId, Long poiId,
                                 String name, String posterUrl, String ticketNotice,
                                 SaleStatusEnum saleStatus, boolean realName, boolean seatSelection,
                                 ZoneId timezone, LocalDateTime earliestSessionStartTime,
                                 LocalDateTime latestSessionStartTime, StockModeEnum stockMode) {
        this.id = id;
        this.supplierId = supplierId;
        this.supplierProjectId = supplierProjectId;
        this.poiId = poiId;
        this.name = name;
        this.posterUrl = posterUrl;
        this.ticketNotice = ticketNotice;
        this.saleStatus = saleStatus;
        this.realName = realName;
        this.seatSelection = seatSelection;
        this.timezone = timezone;
        this.earliestSessionStartTime = earliestSessionStartTime;
        this.latestSessionStartTime = latestSessionStartTime;
        this.stockMode = stockMode;
    }

    public static ApiProjectAggregate pull(Long supplierId, RawProjectVO raw, StockModeEnum stockMode) {
        return new ApiProjectAggregate(null, supplierId, raw.supplierProjectId(), null,
                raw.name(), raw.posterUrl(), raw.ticketNotice(), raw.saleStatus(),
                raw.realName(), raw.seatSelection(), raw.timezone(), null, null, stockMode);
    }

    public static ApiProjectAggregate restore(Long id, Long supplierId, String supplierProjectId, Long poiId,
                                               String name, String posterUrl, String ticketNotice,
                                               SaleStatusEnum saleStatus, boolean realName, boolean seatSelection,
                                               ZoneId timezone, LocalDateTime earliestSessionStartTime,
                                               LocalDateTime latestSessionStartTime, StockModeEnum stockMode) {
        return new ApiProjectAggregate(id, supplierId, supplierProjectId, poiId,
                name, posterUrl, ticketNotice, saleStatus, realName, seatSelection, timezone,
                earliestSessionStartTime, latestSessionStartTime, stockMode);
    }

    public void refresh(RawProjectVO raw) {
        this.name = raw.name();
        this.posterUrl = raw.posterUrl();
        this.ticketNotice = raw.ticketNotice();
        this.saleStatus = raw.saleStatus();
        this.realName = raw.realName();
        this.seatSelection = raw.seatSelection();
        this.timezone = raw.timezone();
    }

    public void bindPoi(Long poiId) {
        if (poiId == null || poiId <= 0) {
            throw new IllegalArgumentException("POI ID 不合法");
        }
        this.poiId = poiId;
    }

    public boolean hasPoi() {
        return poiId != null;
    }

    /**
     * 用本次拉取的场次全量对齐。本次没返回的场次连同其票品一起停售。
     * 场次起止时间由场次本身重算，不采信供应商项目上的时间。
     */
    public void syncSessions(List<RawSessionVO> raws) {
        List<String> incoming = new ArrayList<>();
        for (RawSessionVO raw : raws) {
            incoming.add(raw.supplierSessionId());
            ApiSessionEntity existing = sessions.get(raw.supplierSessionId());
            if (existing == null) {
                ApiSessionEntity created = ApiSessionEntity.pull(supplierId, raw);
                created.bindProject(id);
                sessions.put(raw.supplierSessionId(), created);
            } else {
                existing.refresh(raw);
                existing.bindProject(id);
            }
        }
        sessions.values().stream()
                .filter(session -> !incoming.contains(session.getSupplierSessionId()))
                .forEach(ApiSessionEntity::stopSale);
        recomputeSessionWindow();
    }

    public void attach(ApiSessionEntity session) {
        sessions.put(session.getSupplierSessionId(), session);
    }

    public void assignId(Long id) {
        this.id = id;
        sessions.values().forEach(session -> session.bindProject(id));
    }

    public void assignSessionId(String supplierSessionId, Long sessionId) {
        ApiSessionEntity session = sessions.get(supplierSessionId);
        if (session == null) {
            throw new IllegalArgumentException("场次不存在: " + supplierSessionId);
        }
        session.assignId(sessionId);
    }

    private void recomputeSessionWindow() {
        this.earliestSessionStartTime = sessions.values().stream()
                .map(ApiSessionEntity::getSessionStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        this.latestSessionStartTime = sessions.values().stream()
                .map(ApiSessionEntity::getSessionStartTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    public Long getId() {
        return id;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public String getSupplierProjectId() {
        return supplierProjectId;
    }

    public Long getPoiId() {
        return poiId;
    }

    public String getName() {
        return name;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getTicketNotice() {
        return ticketNotice;
    }

    public SaleStatusEnum getSaleStatus() {
        return saleStatus;
    }

    public boolean isRealName() {
        return realName;
    }

    public boolean isSeatSelection() {
        return seatSelection;
    }

    public ZoneId getTimezone() {
        return timezone;
    }

    public LocalDateTime getEarliestSessionStartTime() {
        return earliestSessionStartTime;
    }

    public LocalDateTime getLatestSessionStartTime() {
        return latestSessionStartTime;
    }

    public StockModeEnum getStockMode() {
        return stockMode;
    }

    public List<ApiSessionEntity> getSessions() {
        return List.copyOf(sessions.values());
    }

}

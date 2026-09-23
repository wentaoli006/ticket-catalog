package com.owen.domain.supplier.model.entity;

import com.owen.domain.supplier.model.valobj.RawSession;
import com.owen.domain.supplier.model.valobj.RawTicketItem;
import com.owen.types.enums.SaleStatusEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 原始场次。票品是场次的子实体，只能通过本对象增删改。
 */
public class ApiSession {

    private Long id;
    private final Long supplierId;
    private Long projectId;
    private final String supplierSessionId;
    private String name;
    private LocalDateTime sessionStartTime;
    private LocalDateTime sessionEndTime;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
    private boolean realName;
    private boolean seatSelection;
    private SaleStatusEnum status;
    private final Map<String, ApiTicketItem> ticketItems = new LinkedHashMap<>();

    private ApiSession(Long id, Long supplierId, Long projectId, String supplierSessionId, String name,
                        LocalDateTime sessionStartTime, LocalDateTime sessionEndTime,
                        LocalDateTime saleStartTime, LocalDateTime saleEndTime,
                        boolean realName, boolean seatSelection, SaleStatusEnum status) {
        this.id = id;
        this.supplierId = supplierId;
        this.projectId = projectId;
        this.supplierSessionId = supplierSessionId;
        this.name = name;
        this.sessionStartTime = sessionStartTime;
        this.sessionEndTime = sessionEndTime;
        this.saleStartTime = saleStartTime;
        this.saleEndTime = saleEndTime;
        this.realName = realName;
        this.seatSelection = seatSelection;
        this.status = status;
    }

    public static ApiSession pull(Long supplierId, RawSession raw) {
        return new ApiSession(null, supplierId, null, raw.supplierSessionId(), raw.name(),
                raw.sessionStartTime(), raw.sessionEndTime(), raw.saleStartTime(), raw.saleEndTime(),
                raw.realName(), raw.seatSelection(), raw.status());
    }

    public static ApiSession restore(Long id, Long supplierId, Long projectId, String supplierSessionId, String name,
                                      LocalDateTime sessionStartTime, LocalDateTime sessionEndTime,
                                      LocalDateTime saleStartTime, LocalDateTime saleEndTime,
                                      boolean realName, boolean seatSelection, SaleStatusEnum status) {
        return new ApiSession(id, supplierId, projectId, supplierSessionId, name,
                sessionStartTime, sessionEndTime, saleStartTime, saleEndTime, realName, seatSelection, status);
    }

    public void refresh(RawSession raw) {
        this.name = raw.name();
        this.sessionStartTime = raw.sessionStartTime();
        this.sessionEndTime = raw.sessionEndTime();
        this.saleStartTime = raw.saleStartTime();
        this.saleEndTime = raw.saleEndTime();
        this.realName = raw.realName();
        this.seatSelection = raw.seatSelection();
        this.status = raw.status();
    }

    public void stopSale() {
        this.status = SaleStatusEnum.OFF_SALE;
        ticketItems.values().forEach(ApiTicketItem::stopSale);
    }

    /**
     * 用本次拉取的票品全量对齐：已有的刷新，没有的新增，本次没返回的停售。
     */
    public void syncTicketItems(List<RawTicketItem> raws) {
        List<String> incoming = new ArrayList<>();
        for (RawTicketItem raw : raws) {
            incoming.add(raw.supplierTicketItemId());
            ApiTicketItem existing = ticketItems.get(raw.supplierTicketItemId());
            if (existing == null) {
                ApiTicketItem created = ApiTicketItem.pull(supplierId, raw);
                created.bindSession(id);
                ticketItems.put(raw.supplierTicketItemId(), created);
            } else {
                existing.refresh(raw);
                existing.bindSession(id);
            }
        }
        ticketItems.values().stream()
                .filter(item -> !incoming.contains(item.getSupplierTicketItemId()))
                .forEach(ApiTicketItem::stopSale);
    }

    void bindProject(Long projectId) {
        this.projectId = projectId;
    }

    void assignId(Long id) {
        this.id = id;
        ticketItems.values().forEach(item -> item.bindSession(id));
    }

    public void attach(ApiTicketItem ticketItem) {
        ticketItems.put(ticketItem.getSupplierTicketItemId(), ticketItem);
    }

    public Long getId() {
        return id;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getSupplierSessionId() {
        return supplierSessionId;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getSessionStartTime() {
        return sessionStartTime;
    }

    public LocalDateTime getSessionEndTime() {
        return sessionEndTime;
    }

    public LocalDateTime getSaleStartTime() {
        return saleStartTime;
    }

    public LocalDateTime getSaleEndTime() {
        return saleEndTime;
    }

    public boolean isRealName() {
        return realName;
    }

    public boolean isSeatSelection() {
        return seatSelection;
    }

    public SaleStatusEnum getStatus() {
        return status;
    }

    public List<ApiTicketItem> getTicketItems() {
        return List.copyOf(ticketItems.values());
    }

}

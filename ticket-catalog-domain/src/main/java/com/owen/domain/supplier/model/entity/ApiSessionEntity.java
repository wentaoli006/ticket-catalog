package com.owen.domain.supplier.model.entity;

import com.owen.domain.supplier.model.valobj.RawSessionVO;
import com.owen.domain.supplier.model.valobj.RawTicketItemVO;
import com.owen.types.enums.SaleStatusEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 原始场次。票品是场次的子实体，只能通过本对象增删改。
 */
public class ApiSessionEntity {

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
    private final Map<String, ApiTicketItemEntity> ticketItems = new LinkedHashMap<>();

    private ApiSessionEntity(Long id, Long supplierId, Long projectId, String supplierSessionId, String name,
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

    public static ApiSessionEntity pull(Long supplierId, RawSessionVO raw) {
        return new ApiSessionEntity(null, supplierId, null, raw.supplierSessionId(), raw.name(),
                raw.sessionStartTime(), raw.sessionEndTime(), raw.saleStartTime(), raw.saleEndTime(),
                raw.realName(), raw.seatSelection(), raw.status());
    }

    public static ApiSessionEntity restore(Long id, Long supplierId, Long projectId, String supplierSessionId, String name,
                                            LocalDateTime sessionStartTime, LocalDateTime sessionEndTime,
                                            LocalDateTime saleStartTime, LocalDateTime saleEndTime,
                                            boolean realName, boolean seatSelection, SaleStatusEnum status) {
        return new ApiSessionEntity(id, supplierId, projectId, supplierSessionId, name,
                sessionStartTime, sessionEndTime, saleStartTime, saleEndTime, realName, seatSelection, status);
    }

    public void refresh(RawSessionVO raw) {
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
        ticketItems.values().forEach(ApiTicketItemEntity::stopSale);
    }

    /**
     * 用本次拉取的票品全量对齐：已有的刷新，没有的新增，本次没返回的停售。
     */
    public void syncTicketItems(List<RawTicketItemVO> raws) {
        List<String> incoming = new ArrayList<>();
        for (RawTicketItemVO raw : raws) {
            incoming.add(raw.supplierTicketItemId());
            ApiTicketItemEntity existing = ticketItems.get(raw.supplierTicketItemId());
            if (existing == null) {
                ApiTicketItemEntity created = ApiTicketItemEntity.pull(supplierId, raw);
                created.bindSession(id);
                ticketItems.put(raw.supplierTicketItemId(), created);
            } else {
                existing.refresh(raw);
                existing.bindSession(id);
            }
        }
        ticketItems.values().stream()
                .filter(item -> !incoming.contains(item.getSupplierTicketItemId()))
                .forEach(ApiTicketItemEntity::stopSale);
    }

    /**
     * 回填所属项目。只由聚合根在同步场次或拿到项目主键后调用。
     */
    public void bindProject(Long projectId) {
        this.projectId = projectId;
    }

    /**
     * 回填场次主键，并同步到下属票品。只由聚合根在落库后调用。
     */
    public void assignId(Long id) {
        this.id = id;
        ticketItems.values().forEach(item -> item.bindSession(id));
    }

    public void attach(ApiTicketItemEntity ticketItem) {
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

    public List<ApiTicketItemEntity> getTicketItems() {
        return List.copyOf(ticketItems.values());
    }

}

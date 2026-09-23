package com.owen.domain.supplier.model.entity;

import com.owen.domain.supplier.model.valobj.RawTicketItem;
import com.owen.types.dp.Money;
import com.owen.types.enums.SaleStatusEnum;

/**
 * 原始票品。展示库存只随供应商数据刷新，不在本系统扣减。
 */
public class ApiTicketItem {

    private Long id;
    private final Long supplierId;
    private final String supplierTicketItemId;
    private Long sessionId;
    private String name;
    private String description;
    private int minSaleQty;
    private int maxSaleQty;
    private Money price;
    private int stockShowNum;
    private String plu;
    private Long pluId;
    private SaleStatusEnum status;
    private int personCount;

    private ApiTicketItem(Long id, Long supplierId, String supplierTicketItemId, Long sessionId,
                           String name, String description, int minSaleQty, int maxSaleQty,
                           Money price, int stockShowNum, String plu, Long pluId,
                           SaleStatusEnum status, int personCount) {
        this.id = id;
        this.supplierId = supplierId;
        this.supplierTicketItemId = supplierTicketItemId;
        this.sessionId = sessionId;
        this.name = name;
        this.description = description;
        this.minSaleQty = minSaleQty;
        this.maxSaleQty = maxSaleQty;
        this.price = price;
        this.stockShowNum = stockShowNum;
        this.plu = plu;
        this.pluId = pluId;
        this.status = status;
        this.personCount = personCount;
    }

    public static ApiTicketItem pull(Long supplierId, RawTicketItem raw) {
        return new ApiTicketItem(null, supplierId, raw.supplierTicketItemId(), null,
                raw.name(), raw.description(), raw.minSaleQty(), raw.maxSaleQty(),
                raw.price(), raw.stockShowNum(), raw.plu(), null, raw.status(), raw.personCount());
    }

    public static ApiTicketItem restore(Long id, Long supplierId, String supplierTicketItemId, Long sessionId,
                                         String name, String description, int minSaleQty, int maxSaleQty,
                                         Money price, int stockShowNum, String plu, Long pluId,
                                         SaleStatusEnum status, int personCount) {
        return new ApiTicketItem(id, supplierId, supplierTicketItemId, sessionId,
                name, description, minSaleQty, maxSaleQty, price, stockShowNum, plu, pluId, status, personCount);
    }

    /**
     * 用本次拉取结果覆盖可变字段。聚合后的 pluId 不属于原始数据，保留不动。
     */
    public void refresh(RawTicketItem raw) {
        this.name = raw.name();
        this.description = raw.description();
        this.minSaleQty = raw.minSaleQty();
        this.maxSaleQty = raw.maxSaleQty();
        this.price = raw.price();
        this.stockShowNum = raw.stockShowNum();
        this.plu = raw.plu();
        this.status = raw.status();
        this.personCount = raw.personCount();
    }

    public void stopSale() {
        this.status = SaleStatusEnum.OFF_SALE;
    }

    void bindSession(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getId() {
        return id;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public String getSupplierTicketItemId() {
        return supplierTicketItemId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getMinSaleQty() {
        return minSaleQty;
    }

    public int getMaxSaleQty() {
        return maxSaleQty;
    }

    public Money getPrice() {
        return price;
    }

    public int getStockShowNum() {
        return stockShowNum;
    }

    public String getPlu() {
        return plu;
    }

    public Long getPluId() {
        return pluId;
    }

    public SaleStatusEnum getStatus() {
        return status;
    }

    public int getPersonCount() {
        return personCount;
    }

}

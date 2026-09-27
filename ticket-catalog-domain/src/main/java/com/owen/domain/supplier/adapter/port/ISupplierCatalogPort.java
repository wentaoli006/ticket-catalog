package com.owen.domain.supplier.adapter.port;

import com.owen.domain.supplier.model.valobj.RawProject;
import com.owen.domain.supplier.model.valobj.RawSession;
import com.owen.domain.supplier.model.valobj.RawTicketItem;
import com.owen.types.enums.SupplierEnum;

import java.util.List;

/**
 * 供应商原始数据出站端口。每个供应商一份实现，domain 只看见标准化后的值对象。
 */
public interface ISupplierCatalogPort {

    SupplierEnum supplier();

    List<RawProject> listProjects();

    List<RawSession> listSessions(String supplierProjectId, boolean seatSelection);

    List<RawTicketItem> listTicketItems(String supplierSessionId, int maxSaleQty);

}

package com.owen.domain.supplier.service;

import com.owen.domain.supplier.acl.PoiPort;
import com.owen.domain.supplier.acl.SupplierCatalogPort;
import com.owen.domain.supplier.model.aggregate.ApiProjectAggregate;
import com.owen.domain.supplier.model.entity.ApiSession;
import com.owen.domain.supplier.model.entity.Poi;
import com.owen.domain.supplier.model.valobj.RawProject;
import com.owen.domain.supplier.model.valobj.RawSession;
import com.owen.domain.supplier.model.valobj.RawTicketItem;
import com.owen.domain.supplier.model.valobj.VenueSnapshot;
import com.owen.domain.supplier.repository.IApiProjectRepository;
import com.owen.domain.supplier.repository.IPoiRepository;
import com.owen.types.enums.StockModeEnum;
import com.owen.types.enums.SupplierEnum;
import com.owen.types.exception.AppException;

import java.util.List;

/**
 * 供应商静态拉取。供应商差异只体现在 CatalogPort，落库和 POI 绑定对所有供应商一致。
 * 展示库存只写入票品，不做真实扣减。
 */
public class SupplierStaticPullService {

    private static final String ERR_NO_PORT = "SUPPLIER_PORT_MISSING";

    private final List<SupplierCatalogPort> catalogPorts;
    private final PoiPort poiPort;
    private final IApiProjectRepository projectRepository;
    private final IPoiRepository poiRepository;

    public SupplierStaticPullService(List<SupplierCatalogPort> catalogPorts,
                                     PoiPort poiPort,
                                     IApiProjectRepository projectRepository,
                                     IPoiRepository poiRepository) {
        this.catalogPorts = catalogPorts;
        this.poiPort = poiPort;
        this.projectRepository = projectRepository;
        this.poiRepository = poiRepository;
    }

    /**
     * 只拉项目。新项目申请 POI 后落库，已有项目只刷新项目字段，不动场次和票品。
     */
    public void pullProjects(SupplierEnum supplier) {
        SupplierCatalogPort port = portOf(supplier);
        for (RawProject raw : port.listProjects()) {
            projectRepository.find(supplier.getId(), raw.supplierProjectId())
                    .ifPresentOrElse(
                            existing -> refreshProject(existing, raw),
                            () -> createProject(supplier, raw));
        }
    }

    /**
     * 按已落库项目逐个拉场次和票品。本次没返回的下级停售，不物理删除。
     */
    public void pullChildren(SupplierEnum supplier) {
        SupplierCatalogPort port = portOf(supplier);
        for (ApiProjectAggregate project : projectRepository.findWithChildren(supplier.getId())) {
            List<RawSession> sessions = port.listSessions(project.getSupplierProjectId(), project.isSeatSelection());
            project.syncSessions(sessions);
            for (RawSession rawSession : sessions) {
                ApiSession session = project.getSessions().stream()
                        .filter(item -> item.getSupplierSessionId().equals(rawSession.supplierSessionId()))
                        .findFirst()
                        .orElseThrow();
                List<RawTicketItem> ticketItems = port.listTicketItems(rawSession.supplierSessionId(), rawSession.maxSaleQty());
                session.syncTicketItems(ticketItems);
                if (session.getStatus() == com.owen.types.enums.SaleStatusEnum.OFF_SALE) {
                    session.stopSale();
                }
            }
            projectRepository.syncChildren(project);
        }
    }

    private void refreshProject(ApiProjectAggregate existing, RawProject raw) {
        existing.refresh(raw);
        projectRepository.save(existing);
    }

    private void createProject(SupplierEnum supplier, RawProject raw) {
        ApiProjectAggregate project = ApiProjectAggregate.pull(supplier.getId(), raw, StockModeEnum.SHARED);
        VenueSnapshot venue = VenueSnapshot.of(raw.venueName(), raw.venueAddress());
        if (venue != null) {
            Long poiId = poiRepository.findByVenue(supplier.getId(), venue.name())
                    .map(Poi::getPoiId)
                    .orElseGet(() -> {
                        Long applied = poiPort.apply(supplier.getName(), raw.supplierProjectId());
                        poiRepository.save(Poi.applied(supplier.getId(), venue, applied));
                        return applied;
                    });
            project.bindPoi(poiId);
        }
        projectRepository.save(project);
    }

    private SupplierCatalogPort portOf(SupplierEnum supplier) {
        return catalogPorts.stream()
                .filter(port -> port.supplier() == supplier)
                .findFirst()
                .orElseThrow(() -> new AppException(ERR_NO_PORT, "未接入供应商: " + supplier.getName()));
    }

}

package com.owen.domain.supplier.service;

import com.owen.domain.supplier.adapter.port.IPoiPort;
import com.owen.domain.supplier.adapter.port.ISupplierCatalogPort;
import com.owen.domain.supplier.model.aggregate.ApiProjectAggregate;
import com.owen.domain.supplier.model.entity.PoiEntity;
import com.owen.domain.supplier.model.valobj.RawProjectVO;
import com.owen.domain.supplier.model.valobj.RawSessionVO;
import com.owen.domain.supplier.model.valobj.VenueSnapshotVO;
import com.owen.domain.supplier.repository.IApiProjectRepository;
import com.owen.domain.supplier.repository.IPoiRepository;
import com.owen.types.enums.SaleStatusEnum;
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

    private final List<ISupplierCatalogPort> catalogPorts;
    private final IPoiPort poiPort;
    private final IApiProjectRepository projectRepository;
    private final IPoiRepository poiRepository;

    public SupplierStaticPullService(List<ISupplierCatalogPort> catalogPorts,
                                     IPoiPort poiPort,
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
        ISupplierCatalogPort port = portOf(supplier);
        for (RawProjectVO raw : port.listProjects()) {
            projectRepository.find(supplier.getId(), raw.supplierProjectId())
                    .ifPresentOrElse(
                            existing -> refreshProject(existing, raw),
                            () -> createProject(supplier, raw));
        }
    }

    /**
     * 本分片在售项目的主键。停售项目不再拉下级。不加载场次和票品。
     */
    public List<Long> listProjectIds(SupplierEnum supplier, int shardIndex, int shardTotal) {
        return projectRepository.findIds(supplier.getId(), SaleStatusEnum.ON_SALE, shardIndex, shardTotal);
    }

    /**
     * 按主键拉取一个项目的下级并落库。场次只向供应商要一次。
     * 内存里只放本次返回的场次，加上库里仍在售、这次没返回的场次。已停售的历史不进聚合。
     */
    public void pullProjectChildren(Long projectId) {
        ApiProjectAggregate project = projectRepository.find(projectId)
                .orElseThrow(() -> new AppException("SUPPLIER_PROJECT_MISSING", "项目不存在: " + projectId));
        ISupplierCatalogPort port = portOf(SupplierEnum.of(project.getSupplierId()));
        List<RawSessionVO> sessions = port.listSessions(project);
        ApiProjectAggregate working = projectRepository.findForChildPull(projectId, sessionIds(sessions))
                .orElseThrow(() -> new AppException("SUPPLIER_PROJECT_MISSING", "项目不存在: " + projectId));
        port.pullChildren(working, sessions);
        projectRepository.syncChildren(working);
    }

    private static List<String> sessionIds(List<RawSessionVO> sessions) {
        return sessions.stream().map(RawSessionVO::supplierSessionId).toList();
    }

    private void refreshProject(ApiProjectAggregate existing, RawProjectVO raw) {
        existing.refresh(raw);
        projectRepository.save(existing);
    }

    private void createProject(SupplierEnum supplier, RawProjectVO raw) {
        ApiProjectAggregate project = ApiProjectAggregate.pull(supplier.getId(), raw, StockModeEnum.SHARED);
        VenueSnapshotVO venue = VenueSnapshotVO.of(raw.venueName(), raw.venueAddress());
        if (venue != null) {
            Long poiId = poiRepository.findByVenue(supplier.getId(), venue.name())
                    .map(PoiEntity::getPoiId)
                    .orElseGet(() -> {
                        Long applied = poiPort.apply(supplier.getName(), raw.supplierProjectId());
                        poiRepository.save(PoiEntity.applied(supplier.getId(), venue, applied));
                        return applied;
                    });
            project.bindPoi(poiId);
        }
        projectRepository.save(project);
    }

    private ISupplierCatalogPort portOf(SupplierEnum supplier) {
        return catalogPorts.stream()
                .filter(port -> port.supplier() == supplier)
                .findFirst()
                .orElseThrow(() -> new AppException(ERR_NO_PORT, "未接入供应商: " + supplier.getName()));
    }

}

package com.owen.infrastructure.adapter.repository;

import com.owen.domain.supplier.model.aggregate.ApiProjectAggregate;
import com.owen.domain.supplier.model.entity.ApiSessionEntity;
import com.owen.domain.supplier.model.entity.ApiTicketItemEntity;
import com.owen.domain.supplier.repository.IApiProjectRepository;
import com.owen.infrastructure.adapter.repository.converter.ApiProjectConverter;
import com.owen.infrastructure.adapter.repository.converter.ApiSessionConverter;
import com.owen.infrastructure.adapter.repository.converter.ApiTicketItemConverter;
import com.owen.infrastructure.dao.IApiProjectDao;
import com.owen.infrastructure.dao.IApiSessionDao;
import com.owen.infrastructure.dao.IApiTicketItemDao;
import com.owen.infrastructure.dao.po.ApiProject;
import com.owen.infrastructure.dao.po.ApiSession;
import com.owen.types.enums.SaleStatusEnum;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 原始项目仓储。项目本身与下级分两次保存，避免只拉项目时改写场次和票品。
 */
@Repository
public class ApiProjectRepository implements IApiProjectRepository {

    private final IApiProjectDao projectDao;
    private final IApiSessionDao sessionDao;
    private final IApiTicketItemDao ticketItemDao;
    private final ApiProjectConverter projectConverter;
    private final ApiSessionConverter sessionConverter;
    private final ApiTicketItemConverter ticketItemConverter;

    public ApiProjectRepository(IApiProjectDao projectDao,
                                 IApiSessionDao sessionDao,
                                 IApiTicketItemDao ticketItemDao,
                                 ApiProjectConverter projectConverter,
                                 ApiSessionConverter sessionConverter,
                                 ApiTicketItemConverter ticketItemConverter) {
        this.projectDao = projectDao;
        this.sessionDao = sessionDao;
        this.ticketItemDao = ticketItemDao;
        this.projectConverter = projectConverter;
        this.sessionConverter = sessionConverter;
        this.ticketItemConverter = ticketItemConverter;
    }

    @Override
    public Optional<ApiProjectAggregate> find(Long supplierId, String supplierProjectId) {
        return Optional.ofNullable(projectConverter.toAggregate(
                projectDao.selectBySupplierProjectId(supplierId, supplierProjectId)));
    }

    @Override
    public Optional<ApiProjectAggregate> find(Long id) {
        return Optional.ofNullable(projectConverter.toAggregate(projectDao.selectById(id)));
    }

    @Override
    public List<ApiProjectAggregate> findBySupplier(Long supplierId) {
        return projectDao.selectBySupplierId(supplierId).stream()
                .map(projectConverter::toAggregate)
                .toList();
    }

    @Override
    public List<Long> findIds(Long supplierId, SaleStatusEnum saleStatus, int shardIndex, int shardTotal) {
        return projectDao.selectIds(supplierId, saleStatus.getCode(), shardIndex, shardTotal);
    }

    @Override
    public Optional<ApiProjectAggregate> findForChildPull(Long id, List<String> supplierSessionIds) {
        return find(id).map(project -> withChildren(project, supplierSessionIds));
    }

    @Override
    public void save(ApiProjectAggregate project) {
        ApiProject po = projectConverter.toPo(project);
        if (po.getId() == null) {
            projectDao.insert(po);
            project.assignId(po.getId());
        } else {
            projectDao.update(po);
        }
    }

    @Override
    public void syncChildren(ApiProjectAggregate project) {
        List<ApiSession> newSessions = new ArrayList<>();
        for (ApiSessionEntity session : project.getSessions()) {
            ApiSession sessionPo = sessionConverter.toPo(session);
            if (sessionPo.getId() == null) {
                newSessions.add(sessionPo);
            } else {
                sessionDao.update(sessionPo);
            }
        }
        if (!newSessions.isEmpty()) {
            sessionDao.insertBatch(newSessions);
            for (ApiSession sessionPo : newSessions) {
                project.assignSessionId(sessionPo.getSupplierSessionId(), sessionPo.getId());
            }
        }
        List<com.owen.infrastructure.dao.po.ApiTicketItem> newItems = new ArrayList<>();
        for (ApiSessionEntity session : project.getSessions()) {
            for (ApiTicketItemEntity item : session.getTicketItems()) {
                com.owen.infrastructure.dao.po.ApiTicketItem itemPo = ticketItemConverter.toPo(item);
                if (itemPo.getId() == null) {
                    newItems.add(itemPo);
                } else {
                    ticketItemDao.update(itemPo);
                }
            }
        }
        if (!newItems.isEmpty()) {
            ticketItemDao.insertBatch(newItems);
        }
        projectDao.refreshSessionWindow(project.getId());
    }

    private ApiProjectAggregate withChildren(ApiProjectAggregate project, List<String> supplierSessionIds) {
        for (ApiSession sessionPo : sessionDao.selectForChildPull(
                project.getId(), SaleStatusEnum.ON_SALE.getCode(), supplierSessionIds)) {
            ApiSessionEntity session = sessionConverter.toEntity(sessionPo);
            boolean returned = supplierSessionIds.contains(session.getSupplierSessionId());
            if (returned || session.getStatus() == SaleStatusEnum.ON_SALE) {
                for (com.owen.infrastructure.dao.po.ApiTicketItem itemPo : ticketItemDao.selectBySessionId(session.getId())) {
                    session.attach(ticketItemConverter.toEntity(itemPo));
                }
            }
            project.attach(session);
        }
        return project;
    }

}

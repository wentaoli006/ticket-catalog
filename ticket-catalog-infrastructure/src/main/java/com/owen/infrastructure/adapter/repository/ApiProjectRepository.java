package com.owen.infrastructure.adapter.repository;

import com.owen.domain.supplier.model.aggregate.ApiProjectAggregate;
import com.owen.domain.supplier.model.entity.ApiSession;
import com.owen.domain.supplier.model.entity.ApiTicketItem;
import com.owen.domain.supplier.repository.IApiProjectRepository;
import com.owen.infrastructure.adapter.repository.converter.ApiProjectConverter;
import com.owen.infrastructure.adapter.repository.converter.ApiSessionConverter;
import com.owen.infrastructure.adapter.repository.converter.ApiTicketItemConverter;
import com.owen.infrastructure.dao.IApiProjectDao;
import com.owen.infrastructure.dao.IApiSessionDao;
import com.owen.infrastructure.dao.IApiTicketItemDao;
import com.owen.infrastructure.dao.po.ApiProject;
import org.springframework.stereotype.Repository;

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
    public List<ApiProjectAggregate> findBySupplier(Long supplierId) {
        return projectDao.selectBySupplierId(supplierId).stream()
                .map(projectConverter::toAggregate)
                .toList();
    }

    @Override
    public List<String> findSupplierProjectIds(Long supplierId) {
        return projectDao.selectBySupplierId(supplierId).stream()
                .map(ApiProject::getSupplierProjectId)
                .toList();
    }

    @Override
    public Optional<ApiProjectAggregate> findWithChildren(Long supplierId, String supplierProjectId) {
        return find(supplierId, supplierProjectId).map(project -> {
            loadChildren(project);
            return project;
        });
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
        save(project);
        for (ApiSession session : project.getSessions()) {
            com.owen.infrastructure.dao.po.ApiSession sessionPo = sessionConverter.toPo(session);
            if (sessionPo.getId() == null) {
                sessionDao.insert(sessionPo);
                project.assignSessionId(session.getSupplierSessionId(), sessionPo.getId());
            } else {
                sessionDao.update(sessionPo);
            }
            for (ApiTicketItem item : session.getTicketItems()) {
                com.owen.infrastructure.dao.po.ApiTicketItem itemPo = ticketItemConverter.toPo(item);
                if (itemPo.getId() == null) {
                    ticketItemDao.insert(itemPo);
                } else {
                    ticketItemDao.update(itemPo);
                }
            }
        }
    }

    private void loadChildren(ApiProjectAggregate project) {
        for (com.owen.infrastructure.dao.po.ApiSession sessionPo : sessionDao.selectByProjectId(project.getId())) {
            ApiSession session = sessionConverter.toEntity(sessionPo);
            for (com.owen.infrastructure.dao.po.ApiTicketItem itemPo : ticketItemDao.selectBySessionId(session.getId())) {
                session.attach(ticketItemConverter.toEntity(itemPo));
            }
            project.attach(session);
        }
    }

}

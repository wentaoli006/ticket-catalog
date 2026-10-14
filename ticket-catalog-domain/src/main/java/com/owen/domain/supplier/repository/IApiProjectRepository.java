package com.owen.domain.supplier.repository;

import com.owen.domain.supplier.model.aggregate.ApiProjectAggregate;
import com.owen.types.enums.SaleStatusEnum;

import java.util.List;
import java.util.Optional;

/**
 * 原始项目仓储。save 负责项目本身，下级场次票品走 syncChildren。
 */
public interface IApiProjectRepository {

    Optional<ApiProjectAggregate> find(Long supplierId, String supplierProjectId);

    /** 只返回指定售卖状态的供应商项目 ID，下级由调用方按项目再加载。 */
    List<String> findSupplierProjectIds(Long supplierId, SaleStatusEnum saleStatus);

    /** 下级拉取用。带出已落库场次和票品，才能把本次没返回的标成停售。 */
    Optional<ApiProjectAggregate> findWithChildren(Long supplierId, String supplierProjectId);

    List<ApiProjectAggregate> findBySupplier(Long supplierId);

    void save(ApiProjectAggregate project);

    void syncChildren(ApiProjectAggregate project);

}

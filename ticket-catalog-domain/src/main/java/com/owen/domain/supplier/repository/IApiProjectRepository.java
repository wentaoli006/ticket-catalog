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

    Optional<ApiProjectAggregate> find(Long id);

    /**
     * 只返回指定售卖状态、且主键落在该分片的项目主键。
     * 取模用表主键，不用供应商项目号。供应商项目号可能是 UUID 或任意字符串。
     */
    List<Long> findIds(Long supplierId, SaleStatusEnum saleStatus, int shardIndex, int shardTotal);

    /**
     * 下级拉取用。只装本次要对齐的场次：供应商本次返回的，加上库里仍在售、这次没返回的。
     * 已停售的历史场次不进聚合。在售场次带全部票品，才能把本次没返回的票品标停售。
     */
    Optional<ApiProjectAggregate> findForChildPull(Long id, List<String> supplierSessionIds);

    List<ApiProjectAggregate> findBySupplier(Long supplierId);

    void save(ApiProjectAggregate project);

    void syncChildren(ApiProjectAggregate project);

}

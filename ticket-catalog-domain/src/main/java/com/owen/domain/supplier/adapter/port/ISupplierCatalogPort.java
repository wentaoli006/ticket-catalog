package com.owen.domain.supplier.adapter.port;

import com.owen.domain.supplier.model.aggregate.ApiProjectAggregate;
import com.owen.domain.supplier.model.valobj.RawProjectVO;
import com.owen.types.enums.SupplierEnum;

import java.util.List;

/**
 * 供应商原始数据出站端口。每个供应商一份实现，domain 只看见标准化后的值对象。
 * 下级拉什么、按什么顺序拉，由实现自己决定，不在这里规定场次、票品或其他明细。
 */
public interface ISupplierCatalogPort {

    SupplierEnum supplier();

    List<RawProjectVO> listProjects();

    /**
     * 拉取一个已落库项目的下级，并写回该聚合。本次没返回的下级由实现标停售，不物理删除。
     */
    void pullChildren(ApiProjectAggregate project);

}

package com.owen.domain.supplier.adapter.port;

import com.owen.domain.supplier.model.aggregate.ApiProjectAggregate;
import com.owen.domain.supplier.model.valobj.RawProjectVO;
import com.owen.domain.supplier.model.valobj.RawSessionVO;
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
     * 本次要落库的场次。只拉场次，不在这里拉票品或其他明细。
     */
    List<RawSessionVO> listSessions(ApiProjectAggregate project);

    /**
     * 把已经取回的场次写回聚合，再拉场次下的明细。本次没返回的下级标停售，不物理删除。
     */
    void pullChildren(ApiProjectAggregate project, List<RawSessionVO> sessions);

}

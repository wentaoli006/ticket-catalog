package com.owen.infrastructure.dao;

import com.owen.infrastructure.dao.po.ApiSession;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IApiSessionDao {

    int insert(ApiSession apiSession);

    int insertBatch(List<ApiSession> sessions);

    int update(ApiSession apiSession);

    ApiSession selectById(Long id);

    ApiSession selectBySupplierSessionId(Long supplierId, String supplierSessionId);

    List<ApiSession> selectByProjectId(Long projectId);

    /** 本次返回的场次，加上该项目下仍在售的场次。已停售且本次没返回的不查。 */
    List<ApiSession> selectForChildPull(Long projectId, Integer onSale, List<String> supplierSessionIds);

}

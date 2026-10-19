package com.owen.infrastructure.dao;

import com.owen.infrastructure.dao.po.ApiProject;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IApiProjectDao {

    int insert(ApiProject apiProject);

    int update(ApiProject apiProject);

    ApiProject selectById(Long id);

    ApiProject selectBySupplierProjectId(Long supplierId, String supplierProjectId);

    List<ApiProject> selectBySupplierId(Long supplierId);

    List<Long> selectIds(Long supplierId, Integer saleStatus, int shardIndex, int shardTotal);

    int refreshSessionWindow(Long projectId);

}

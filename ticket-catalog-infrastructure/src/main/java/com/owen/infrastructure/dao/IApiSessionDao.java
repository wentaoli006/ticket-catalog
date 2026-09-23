package com.owen.infrastructure.dao;

import com.owen.infrastructure.dao.po.ApiSession;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IApiSessionDao {

    int insert(ApiSession apiSession);

    int update(ApiSession apiSession);

    ApiSession selectById(Long id);

    ApiSession selectBySupplierSessionId(Long supplierId, String supplierSessionId);

    List<ApiSession> selectByProjectId(Long projectId);

}

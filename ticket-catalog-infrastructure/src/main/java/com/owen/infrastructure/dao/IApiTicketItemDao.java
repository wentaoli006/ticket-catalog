package com.owen.infrastructure.dao;

import com.owen.infrastructure.dao.po.ApiTicketItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IApiTicketItemDao {

    int insert(ApiTicketItem apiTicketItem);

    int insertBatch(List<ApiTicketItem> items);

    int update(ApiTicketItem apiTicketItem);

    ApiTicketItem selectById(Long id);

    ApiTicketItem selectBySupplierTicketItemId(Long supplierId, String supplierTicketItemId);

    List<ApiTicketItem> selectBySessionId(Long sessionId);

}

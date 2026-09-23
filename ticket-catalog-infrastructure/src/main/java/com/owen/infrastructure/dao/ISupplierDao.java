package com.owen.infrastructure.dao;

import com.owen.infrastructure.dao.po.Supplier;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ISupplierDao {

    int insert(Supplier supplier);

    int update(Supplier supplier);

    Supplier selectById(Long id);

    Supplier selectByName(String name);

    List<Supplier> selectAll();

}

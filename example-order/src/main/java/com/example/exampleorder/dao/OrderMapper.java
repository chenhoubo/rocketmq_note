package com.example.exampleorder.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.exampleorder.model.Order;
import org.apache.ibatis.annotations.Mapper;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单表 Mapper 接口
* @since seeingflow
*/
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}

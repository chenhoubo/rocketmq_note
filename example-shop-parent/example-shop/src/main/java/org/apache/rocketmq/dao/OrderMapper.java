package org.apache.rocketmq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.po.Order;
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

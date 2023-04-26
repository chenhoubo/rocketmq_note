package org.apache.rocketmq.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.po.OrderPay;
import org.apache.ibatis.annotations.Mapper;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单支付表 Mapper 接口
* @since seeingflow
*/
@Mapper
public interface OrderPayMapper extends BaseMapper<OrderPay> {

}

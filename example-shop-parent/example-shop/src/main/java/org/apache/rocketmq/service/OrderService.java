package org.apache.rocketmq.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.Order;
import com.example.domain.to.ConfirmOrderTo;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单表 服务类
* @since seeingflow
*/
public interface OrderService extends IService<Order> {

    ResultMsg<Long> confirmOrder(ConfirmOrderTo confirmOrderTo);
}

package com.example.exampleorder.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.exampleorder.model.Order;
import com.example.exampleorder.utils.ResultMsg;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单表 服务类
* @since seeingflow
*/
public interface OrderService extends IService<Order> {

    ResultMsg addOrder(Order order);
}

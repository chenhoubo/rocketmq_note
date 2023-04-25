package com.example.exampleorder.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.exampleorder.dao.OrderMapper;
import com.example.exampleorder.model.Order;
import com.example.exampleorder.service.OrderService;
import com.example.exampleorder.utils.ResultMsg;
import org.springframework.stereotype.Service;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单表 服务实现类
* @since seeingflow
*/
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    public ResultMsg addOrder(Order order) {
        baseMapper.insert(order);
        return ResultMsg.success();
    }

}

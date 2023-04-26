package org.apache.rocketmq.controller;

import com.example.common.utils.ResultMsg;
import com.example.domain.po.Order;
import com.example.domain.to.ConfirmOrderTo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单表 前端控制器
* @since seeingflow
*/
@Slf4j
@Api(value = "OrderController",tags = "订单表")
@RestController
@RequestMapping("/order")
    public class OrderController {

    @Autowired
    public OrderService orderService;

    @ApiOperation("确认订单")
    @PostMapping("/confirmOrder")
    public ResultMsg<Long> confirmOrder(@RequestBody ConfirmOrderTo confirmOrderTo){
        return orderService.confirmOrder(confirmOrderTo);
    }


}

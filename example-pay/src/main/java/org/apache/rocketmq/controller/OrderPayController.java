package org.apache.rocketmq.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.model.OrderPay;
import org.apache.rocketmq.service.OrderPayService;
import org.apache.rocketmq.utils.ResultMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单支付表 前端控制器
* @since seeingflow
*/
@Slf4j
@Api(value = "OrderPayController",tags = "订单支付表")
@RestController
@RequestMapping("/orderPay")
    public class OrderPayController {

    @Autowired
    public OrderPayService orderPayService;


    @ApiOperation("添加订单支付记录")
    @PostMapping("/addOrderPay")
    public ResultMsg addOrderPay(@RequestBody OrderPay orderPay){
        return orderPayService.addOrderPay(orderPay);
    }

}

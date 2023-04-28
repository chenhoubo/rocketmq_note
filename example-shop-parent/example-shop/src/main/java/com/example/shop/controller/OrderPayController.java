package com.example.shop.controller;

import com.example.common.utils.ResultMsg;
import com.example.domain.po.OrderPay;
import com.example.shop.service.OrderPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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


    @ApiOperation("订单支付")
    @PostMapping("/createPayment")
    public ResultMsg createPayment(@RequestBody OrderPay orderPay){
        return orderPayService.createPayment(orderPay);
    }

    @ApiOperation("更新支付状态")
    @GetMapping("/updateIsPaid")
    public ResultMsg updateIsPaid(@RequestParam("payId") Long payId, @RequestParam("isPaid") Integer isPaid){
        return orderPayService.updateIsPaid(payId, isPaid);
    }

}

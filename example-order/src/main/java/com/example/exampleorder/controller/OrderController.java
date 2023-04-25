package com.example.exampleorder.controller;

import com.example.exampleorder.model.Order;
import com.example.exampleorder.service.OrderService;
import com.example.exampleorder.utils.ResultMsg;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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

    @ApiOperation("创建订单")
    @PostMapping("/addOrder")
    public ResultMsg addOrder(@RequestBody Order order){
        return orderService.addOrder(order);
    }


}

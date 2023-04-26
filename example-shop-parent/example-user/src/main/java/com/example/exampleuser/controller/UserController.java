package com.example.exampleuser.controller;

import com.example.common.utils.ResultMsg;
import com.example.domain.po.Order;
import com.example.domain.po.OrderPay;
import com.example.exampleuser.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 用户表 前端控制器
* @since seeingflow
*/
@Slf4j
@Api(value = "UserController",tags = "用户表")
@RestController
@RequestMapping("/user")
    public class UserController {

    @Autowired
    public UserService userService;

    @ApiOperation("用户下单--提交订单的业务请求")
    @PostMapping("/order/createOrder")
    public ResultMsg createOrder(@RequestBody Order order){
        return userService.createOrder(order);
    }

    @ApiOperation("用户支付--提交支付业务请求")
    @PostMapping("/pay/addOrderPay")
    public ResultMsg addOrderPay(@RequestBody OrderPay orderPay){
        return userService.addOrderPay(orderPay);
    }

    @ApiOperation("查询--用户余额")
    @GetMapping("/getUserMoney")
    public ResultMsg<BigDecimal> getUserMoney(@RequestParam("userId") Long userId){
        return userService.getUserMoney(userId);
    }

    @ApiOperation("查询--添加用户积分")
    @GetMapping("/addUserScore")
    public ResultMsg addUserScore(@RequestParam("userId") Long userId, @RequestParam("userScore") Integer userScore){
        return userService.addUserScore(userId, userScore);
    }

}

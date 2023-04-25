package com.example.exampleuser.controller;

import com.example.exampleuser.model.Order;
import com.example.exampleuser.service.UserService;
import com.example.exampleuser.utils.ResultMsg;
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


}

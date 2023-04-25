package com.example.exampleuser.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.exampleuser.model.Order;
import com.example.exampleuser.model.User;
import com.example.exampleuser.utils.ResultMsg;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 用户表 服务类
* @since seeingflow
*/
public interface UserService extends IService<User> {
    ResultMsg createOrder(Order order);
}

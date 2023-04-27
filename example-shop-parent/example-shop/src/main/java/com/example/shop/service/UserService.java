package com.example.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.Order;
import com.example.domain.po.OrderPay;
import com.example.domain.po.User;
import com.example.domain.po.UserMoneyLog;

import java.math.BigDecimal;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 用户表 服务类
* @since seeingflow
*/
public interface UserService extends IService<User> {
    ResultMsg createOrder(Order order);

    ResultMsg addOrderPay(OrderPay orderPay);

    ResultMsg<BigDecimal> getUserMoney(Long userId);

    ResultMsg addUserScore(Long userId, Integer userScore);

    ResultMsg changeUserMoney(UserMoneyLog userMoneyLog);
}

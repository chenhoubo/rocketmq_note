package com.example.exampleuser.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.exampleuser.dao.UserMapper;
import com.example.exampleuser.model.Order;
import com.example.exampleuser.model.User;
import com.example.exampleuser.service.UserService;
import com.example.exampleuser.utils.ResultMsg;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 用户表 服务实现类
* @since seeingflow
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public ResultMsg createOrder(Order order) {
        //向mq发送创建订单的消息
        String msg = JSONObject.toJSONString(order);
        MessageBuilder<String> stringMessageBuilder = MessageBuilder.withPayload(msg);
        stringMessageBuilder.setHeader("msg",msg);
        Message<String> message = stringMessageBuilder.build();
        rocketMQTemplate.sendMessageInTransaction("orderTopic", message, null);

        //向用户发送
        return ResultMsg.success();
    }
}

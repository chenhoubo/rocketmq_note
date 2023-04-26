package org.apache.rocketmq.service.mq;

import com.alibaba.fastjson.JSONObject;
import com.example.domain.po.Order;
import org.apache.rocketmq.service.OrderService;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/4/25 16:45
 * @Desc
 * @since seeingflow
 */
@Service
@RocketMQMessageListener(topic = "orderTopic", consumerGroup = "orderGroup", messageModel = MessageModel.CLUSTERING)
public class OrderConsumer implements RocketMQListener<String> {
    @Autowired
    private OrderService orderService;

    @Override
    public void onMessage(String msg) {
        Order orderEntity = JSONObject.parseObject(msg, Order.class);
        orderService.addOrder(orderEntity);
    }
}

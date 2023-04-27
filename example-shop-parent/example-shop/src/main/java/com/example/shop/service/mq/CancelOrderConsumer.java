package com.example.shop.service.mq;

import com.alibaba.fastjson.JSON;
import com.example.common.enums.ShopCode;
import com.example.domain.po.Order;
import com.example.domain.to.CancelOrderMQ;
import com.example.shop.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/4/27 11:01
 * @Desc 取消订单-监听
 * @since seeingflow
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.order.topic}",
        consumerGroup = "${mq.order.consumer.group.name}",
        messageModel = MessageModel.BROADCASTING)
public class CancelOrderConsumer implements RocketMQListener<MessageExt> {

    @Autowired
    private OrderService orderService;

    @Override
    public void onMessage(MessageExt messageExt) {
        String body = null;
        try {
            body = new String(messageExt.getBody(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String msgId = messageExt.getMsgId();
        String tags = messageExt.getTags();
        String keys = messageExt.getKeys();
        log.info("CancelOrderProcessor receive message:"+messageExt);
        CancelOrderMQ cancelOrderMQ = JSON.parseObject(body, CancelOrderMQ.class);
        Order order = orderService.getById(cancelOrderMQ.getOrderId());
        order.setOrderStatus(ShopCode.SHOP_ORDER_CANCEL.getCode());
        orderService.updateById(order);
        log.info("订单:["+order.getOrderId()+"]状态设置为取消");
    }
}
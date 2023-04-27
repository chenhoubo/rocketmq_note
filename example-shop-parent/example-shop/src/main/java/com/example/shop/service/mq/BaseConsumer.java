package com.example.shop.service.mq;

import com.alibaba.fastjson.JSON;
import com.example.common.enums.ShopCode;
import com.example.domain.po.Order;
import com.example.domain.to.MQEntity;
import com.example.shop.service.OrderService;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/4/27 15:16
 * @Desc
 * @since seeingflow
 */
public class BaseConsumer {
    public Order handleMessage(OrderService orderService, MessageExt messageExt, Integer code) throws Exception {
        //解析消息内容
        String body = new String(messageExt.getBody(), "UTF-8");
        String msgId = messageExt.getMsgId();
        String tags = messageExt.getTags();
        String keys = messageExt.getKeys();
        MQEntity orderMq = JSON.parseObject(body, MQEntity.class);

        //查询
        Order order = orderService.getById(orderMq.getOrderId());

        if(ShopCode.SHOP_ORDER_MESSAGE_STATUS_CANCEL.getCode() == code){
            order.setOrderStatus(ShopCode.SHOP_ORDER_CANCEL.getCode());
        }

        if(ShopCode.SHOP_ORDER_MESSAGE_STATUS_ISPAID.getCode() == code){
            order.setPayStatus(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());
        }
        orderService.updateById(order);
        return order;
    }
}

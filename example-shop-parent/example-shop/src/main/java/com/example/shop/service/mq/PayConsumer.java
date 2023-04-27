package com.example.shop.service.mq;

import com.example.common.enums.ShopCode;
import com.example.domain.po.Order;
import com.example.shop.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/4/27 15:20
 * @Desc
 * @since seeingflow
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.pay.topic}",
        consumerGroup = "${mq.pay.consumer.group.name}")
public class PayConsumer extends BaseConsumer implements RocketMQListener<MessageExt> {

    @Autowired
    private OrderService orderService;

    @Override
    public void onMessage(MessageExt messageExt) {
        try {
            log.info("CancelOrderProcessor receive message:"+messageExt);
            Order order = handleMessage(orderService,
                    messageExt,
                    ShopCode.SHOP_ORDER_MESSAGE_STATUS_ISPAID.getCode());
            log.info("订单:["+order.getOrderId()+"]支付成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("订单支付失败");
        }
    }
}

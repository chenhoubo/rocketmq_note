package com.example.shop.service.mq;

import com.alibaba.fastjson.JSON;
import com.example.common.enums.ShopCode;
import com.example.domain.po.UserMoneyLog;
import com.example.domain.to.MQEntity;
import com.example.shop.service.UserMoneyLogService;
import com.example.shop.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/4/27 11:01
 * @Desc 退回余额-监听
 * @since seeingflow
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.order.topic}",
        consumerGroup = "${mq.order.consumer.group.name}",
        messageModel = MessageModel.BROADCASTING)
public class CancelUserMoneyMQListener implements RocketMQListener<MessageExt> {

    @Autowired
    private UserMoneyLogService userMoneyLogService;

    @Autowired
    private UserService userService;

    @Override
    public void onMessage(MessageExt messageExt) {

        try {
            //1.解析消息
            String body = new String(messageExt.getBody(), "UTF-8");
            MQEntity mqEntity = JSON.parseObject(body, MQEntity.class);
            log.info("接收到消息");
            if(mqEntity.getUserMoney()!=null && mqEntity.getUserMoney().compareTo(BigDecimal.ZERO)>0){
                //2.调用业务层,进行余额修改
                UserMoneyLog userMoneyLog = new UserMoneyLog();
                userMoneyLog.setUseMoney(mqEntity.getUserMoney());
                userMoneyLog.setMoneyLogType(ShopCode.SHOP_USER_MONEY_REFUND.getCode());
                userMoneyLog.setUserId(mqEntity.getUserId());
                userMoneyLog.setOrderId(mqEntity.getOrderId());
                userMoneyLogService.save(userMoneyLog);
                userService.changeUserMoney(userMoneyLog);
                log.info("余额回退成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("余额回退失败");
        }

    }

}
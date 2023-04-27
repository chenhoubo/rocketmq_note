package com.example.shop.service.mq;//package org.apache.rocketmq.service.mq;
//
//import com.alibaba.fastjson.JSON;
//import com.example.common.enums.ShopCode;
//import com.example.domain.po.Coupon;
//import com.example.domain.to.MQEntity;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.common.message.MessageExt;
//import org.apache.rocketmq.dao.CouponMapper;
//import org.apache.rocketmq.spring.annotation.MessageModel;
//import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
//import org.apache.rocketmq.spring.core.RocketMQListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//
///**
// * @author Chenhoubo
// * @version v1.0
// * @date 2023/4/27 11:01
// * @Desc 优惠券退回-监听
// * @since seeingflow
// */
//@Slf4j
//@Component
//@RocketMQMessageListener(topic = "${mq.order.topic}",
//        consumerGroup = "${mq.order.consumer.group.name}",
//        messageModel = MessageModel.BROADCASTING)
//public class CancelCouponMQListener implements RocketMQListener<MessageExt> {
//
//    @Autowired
//    private CouponMapper couponMapper;
//
//    @Override
//    public void onMessage(MessageExt message) {
//        try {
//            //1. 解析消息内容
//            String body = new String(message.getBody(), "UTF-8");
//            MQEntity mqEntity = JSON.parseObject(body, MQEntity.class);
//            log.info("接收到消息");
//            //2. 查询优惠券信息
//            Coupon coupon = couponMapper.selectById(mqEntity.getCouponId());
//            //3.更改优惠券状态
//            coupon.setUsedTime(null);
//            coupon.setIsUsed(ShopCode.SHOP_COUPON_UNUSED.getCode());
//            coupon.setOrderId(null);
//            couponMapper.updateById(coupon);
//            log.info("回退优惠券成功");
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error("回退优惠券失败");
//        }
//
//    }
//}
package com.example.shop.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.enums.ShopCode;
import com.example.common.exception.ShopException;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.OrderPay;
import com.example.domain.po.ProducerMsg;
import com.example.shop.dao.OrderPayMapper;
import com.example.shop.dao.ProducerMsgMapper;
import com.example.shop.service.OrderPayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单支付表 服务实现类
* @since seeingflow
*/
@Service
@Slf4j
public class OrderPayServiceImpl extends ServiceImpl<OrderPayMapper, OrderPay> implements OrderPayService {

    @Autowired
    private OrderPayMapper orderPayMapper;

    @Value("${mq.pay.topic}")
    private String topic;

    @Value("${mq.pay.tag.pay}")
    private String tag;

    @Autowired
    private ProducerMsgMapper producerMsgMapper;

    @Autowired
    private ThreadPoolTaskExecutor executorService;


    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Transactional
    @Override
    public ResultMsg createPayment(OrderPay orderPay) {
        //查询订单支付状态
        try {
            QueryWrapper<OrderPay> TradePayExample = new QueryWrapper<>();
            TradePayExample.eq("order_id",orderPay.getOrderId())
                            .eq("is_paid", ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());
            int count = baseMapper.selectCount(TradePayExample);
            if (count > 0) {
                ShopException.cast(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY);
            }

            orderPay.setIsPaid(ShopCode.SHOP_ORDER_PAY_STATUS_NO_PAY.getCode());
            baseMapper.insert(orderPay);
            log.info("创建支付订单成功:" + orderPay.getOrderId());
            //这里直接模拟用户已经第三方付款，进行了回调
            orderPay.setIsPaid(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());
            callbackPayment(orderPay);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultMsg.error();
        }
        return ResultMsg.success();
    }

    @Override
    public ResultMsg updateIsPaid(Long payId, Integer isPaid) {
        OrderPay orderPay = new OrderPay();
        orderPay.setPayId(payId);
        orderPay.setIsPaid(isPaid);
        baseMapper.updateById(orderPay);
        return ResultMsg.success();
    }

    //支付回调接口
    public ResultMsg callbackPayment(OrderPay tradePay) {

        if (tradePay.getIsPaid().equals(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode())) {
            tradePay = orderPayMapper.selectById(tradePay.getPayId());
            if (tradePay == null) {
                ShopException.cast(ShopCode.SHOP_PAYMENT_NOT_FOUND);
            }
            tradePay.setIsPaid(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY.getCode());
            int i = orderPayMapper.updateById(tradePay);
            //更新成功代表支付成功
            if (i == 1) {
                ProducerMsg mqProducerTemp = new ProducerMsg();
                mqProducerTemp.setGroupName("payProducerGroup");
                mqProducerTemp.setMsgKey(String.valueOf(tradePay.getPayId()));
                mqProducerTemp.setMsgTag(topic);
                mqProducerTemp.setMsgBody(JSON.toJSONString(tradePay));
                mqProducerTemp.setCreateTime(new Date());
                producerMsgMapper.insert(mqProducerTemp);
                OrderPay finalTradePay = tradePay;
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SendResult sendResult = sendMessage(topic, tag, finalTradePay.getPayId(), JSON.toJSONString(finalTradePay));
                            log.info(JSON.toJSONString(sendResult));
                            if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                                producerMsgMapper.deleteById(mqProducerTemp.getId());
                                System.out.println("删除消息表成功");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                ShopException.cast(ShopCode.SHOP_PAYMENT_IS_PAID);
            }
        }
        return ResultMsg.success();
    }

    private SendResult sendMessage(String topic, String tags, Long payId, String body) throws Exception {
        //判断Topic是否为空
        if (StringUtils.isEmpty(topic)) {
            ShopException.cast(ShopCode.SHOP_MQ_TOPIC_IS_EMPTY);
        }
        //判断消息内容是否为空
        if (StringUtils.isEmpty(body)) {
            ShopException.cast(ShopCode.SHOP_MQ_MESSAGE_BODY_IS_EMPTY);
        }
        //消息体
        Message message = new Message(topic, tags, payId.toString(), body.getBytes());
        //发送消息
        SendResult sendResult = rocketMQTemplate.getProducer().send(message);

        return sendResult;
    }

//    @Bean
//    public ThreadPoolTaskExecutor getThreadPool(){
//
//        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
//
//        executor.setCorePoolSize(4);
//
//        executor.setMaxPoolSize(8);
//
//        executor.setQueueCapacity(100);
//
//        executor.setKeepAliveSeconds(60);
//
//        executor.setThreadNamePrefix("Pool-A");
//
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//
//        executor.initialize();
//
//        return executor;
//
//    }

}

package com.example.shop.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.enums.ShopCode;
import com.example.common.exception.ShopException;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.*;
import com.example.domain.to.CancelOrderMQ;
import com.example.domain.to.ConfirmOrderTo;
import com.example.shop.dao.OrderMapper;
import com.example.shop.service.CouponService;
import com.example.shop.service.GoodsService;
import com.example.shop.service.OrderService;
import com.example.shop.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
* @author seeingflow
* @Date 2023-04-25
* @Desc 订单表 服务实现类
* @since seeingflow
*/
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private UserService userService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Value("${mq.order.topic}")
    private String topic;

    @Value("${mq.order.tag.cancel}")
    private String cancelTag;

    @Transactional
    @Override
    public ResultMsg<Long> confirmOrder(ConfirmOrderTo confirmOrderTo) {
        //1.校验订单
        Order order = checkOrder(confirmOrderTo);
        //2.生成预订单
        Long orderId = savePreOrder(order);
        try {
            //3.扣减库存
            reduceGoodsNum(order);
            //todo 4.扣减优惠券
//            changeCoponStatus(order);
            //5.使用余额
            reduceMoneyPaid(order);
            //6.确认订单
            updateOrderStatus(order);
            //7.返回成功状态
            log.info("订单:["+orderId+"]确认成功");
            return ResultMsg.success(orderId);
        } catch (Exception e) {
            //1.确认订单失败,发送消息
            //确认订单失败,发送消息
            CancelOrderMQ cancelOrderMQ = new CancelOrderMQ();
            cancelOrderMQ.setOrderId(order.getOrderId());
            cancelOrderMQ.setCouponId(order.getCouponId());
            cancelOrderMQ.setGoodsId(order.getGoodsId());
            cancelOrderMQ.setGoodsNumber(order.getGoodsNumber());
            cancelOrderMQ.setUserId(order.getUserId());
            cancelOrderMQ.setUserMoney(order.getMoneyPaid());
            try {
                sendMessage(topic,
                        cancelTag,
                        cancelOrderMQ.getOrderId().toString(),
                        JSON.toJSONString(cancelOrderMQ));
            } catch (Exception e1) {
                e1.printStackTrace();
                ShopException.cast(ShopCode.SHOP_MQ_SEND_MESSAGE_FAIL);
            }
            //2.返回失败状态
            throw e;
        }
    }

    private Order checkOrder(ConfirmOrderTo confirmOrderTo) {
        //1.校验订单是否存在
        if(confirmOrderTo==null){
            ShopException.cast(ShopCode.SHOP_ORDER_INVALID);
        }
        //2.校验订单中的商品是否存在
        Goods goods = goodsService.getById(confirmOrderTo.getGoodsId());
        if(goods==null){
            ShopException.cast(ShopCode.SHOP_GOODS_NO_EXIST);
        }
        //3.校验下单用户是否存在
        User user = userService.getById(confirmOrderTo.getUserId());
        if(user==null){
            ShopException.cast(ShopCode.SHOP_USER_NO_EXIST);
        }
        //4.校验商品单价是否合法
        if(confirmOrderTo.getGoodsPrice().compareTo(goods.getGoodsPrice())!=0){
            ShopException.cast(ShopCode.SHOP_GOODS_PRICE_INVALID);
        }
        //5.校验订单商品数量是否合法
        if(confirmOrderTo.getGoodsNumber()>=goods.getGoodsNumber()){
            ShopException.cast(ShopCode.SHOP_GOODS_NUM_NOT_ENOUGH);
        }
        Order order = new Order();
        BeanUtils.copyProperties(confirmOrderTo, order);
        log.info("校验订单通过");
        return order;
    }

    private Long savePreOrder(Order order) {
        //设置订单状态为不可见
        order.setOrderStatus(ShopCode.SHOP_ORDER_NO_CONFIRM.getCode());
        //计算订单总价格是否正确
        BigDecimal orderAmount = order.getGoodsPrice().multiply(new BigDecimal(order.getGoodsNumber()));
        orderAmount = orderAmount.add(order.getShippingFee());
        if (orderAmount.compareTo(order.getOrderAmount()) != 0) {
            ShopException.cast(ShopCode.SHOP_ORDERAMOUNT_INVALID);
        }
        //判断优惠券信息是否合法
        Long couponId = order.getCouponId();
        if (couponId != null) {
            //。。。。
        } else {
            order.setCouponPaid(BigDecimal.ZERO);
        }

        //判断余额是否正确
        BigDecimal moneyPaid = order.getMoneyPaid();
        if (moneyPaid != null) {
            //比较余额是否大于0
            int r = order.getMoneyPaid().compareTo(BigDecimal.ZERO);
            //余额小于0
            if (r == -1) {
                ShopException.cast(ShopCode.SHOP_MONEY_PAID_LESS_ZERO);
            }
            //余额大于0
            if (r == 1) {
                //查询用户信息
                User user = userService.getById(order.getUserId());
                if (user == null) {
                    ShopException.cast(ShopCode.SHOP_USER_NO_EXIST);
                }
                //比较余额是否大于用户账户余额
                if (user.getUserMoney().compareTo(order.getMoneyPaid()) == -1) {
                    ShopException.cast(ShopCode.SHOP_MONEY_PAID_INVALID);
                }
                order.setMoneyPaid(order.getMoneyPaid());
            }
        } else {
            order.setMoneyPaid(BigDecimal.ZERO);
        }
        //计算订单支付总价
        order.setPayAmount(orderAmount.subtract(order.getCouponPaid())
                .subtract(order.getMoneyPaid()));
        //设置订单添加时间
        order.setAddTime(new Date());

        //保存预订单
        baseMapper.insert(order);
        log.info("订单:["+order.getOrderId()+"]预订单生成成功");
        return order.getOrderId();
    }

    private void reduceGoodsNum(Order order) {
        GoodsOrderLog goodsNumberLog = new GoodsOrderLog();
        goodsNumberLog.setGoodsId(order.getGoodsId());
        goodsNumberLog.setOrderId(order.getOrderId());
        goodsNumberLog.setGoodsNumber(order.getGoodsNumber());
        ResultMsg result = goodsService.reduceGoodsNum(goodsNumberLog);
        if (!result.checkSuccess()) {
            ShopException.cast(ShopCode.SHOP_REDUCE_GOODS_NUM_FAIL);
        }
        log.info("订单:["+order.getOrderId()+"]扣减库存["+order.getGoodsNumber()+"个]成功");
    }

    private void changeCoponStatus(Order order) {
        //判断用户是否使用优惠券
        if (Objects.nonNull(order.getCouponId())) {
            //封装优惠券对象
            Coupon coupon = couponService.getById(order.getCouponId());
            coupon.setIsUsed(ShopCode.SHOP_COUPON_ISUSED.getCode());
            coupon.setUsedTime(new Date());
            coupon.setOrderId(order.getOrderId());
            ResultMsg result = couponService.changeCouponStatus(coupon);
            //判断执行结果
            if (result.checkSuccess()) {
                //优惠券使用失败
                ShopException.cast(ShopCode.SHOP_COUPON_USE_FAIL);
            }
            log.info("订单:["+order.getOrderId()+"]使用扣减优惠券["+coupon.getCouponPrice()+"元]成功");
        }
    }


    private void reduceMoneyPaid(Order order) {
        //判断订单中使用的余额是否合法
        if (order.getMoneyPaid() != null && order.getMoneyPaid().compareTo(BigDecimal.ZERO) == 1) {
            UserMoneyLog userMoneyLog = new UserMoneyLog();
            userMoneyLog.setOrderId(order.getOrderId());
            userMoneyLog.setUserId(order.getUserId());
            userMoneyLog.setUseMoney(order.getMoneyPaid());
            userMoneyLog.setMoneyLogType(ShopCode.SHOP_USER_MONEY_PAID.getCode());
            //扣减余额
            ResultMsg result = userService.changeUserMoney(userMoneyLog);
            if (result.checkSuccess()) {
                ShopException.cast(ShopCode.SHOP_USER_MONEY_REDUCE_FAIL);
            }
            log.info("订单:["+order.getOrderId()+"扣减余额["+order.getMoneyPaid()+"元]成功]");
        }
    }

    private void updateOrderStatus(Order order) {
        order.setOrderStatus(ShopCode.SHOP_ORDER_CONFIRM.getCode());
        order.setPayStatus(ShopCode.SHOP_ORDER_PAY_STATUS_NO_PAY.getCode());
        order.setConfirmTime(new Date());
        int r = baseMapper.updateById(order);
        if (r <= 0) {
            ShopException.cast(ShopCode.SHOP_ORDER_CONFIRM_FAIL);
        }
        log.info("订单:["+order.getOrderId()+"]状态修改成功");
    }

    private void sendMessage(String topic, String tags, String keys, String body) throws Exception {
        //判断Topic是否为空
        if (StringUtils.isEmpty(topic)) {
            ShopException.cast(ShopCode.SHOP_MQ_TOPIC_IS_EMPTY);
        }
        //判断消息内容是否为空
        if (StringUtils.isEmpty(body)) {
            ShopException.cast(ShopCode.SHOP_MQ_MESSAGE_BODY_IS_EMPTY);
        }
        //消息体
        Message message = new Message(topic, tags, keys, body.getBytes());
        //发送消息
        rocketMQTemplate.getProducer().send(message);
    }

}

package org.apache.rocketmq.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.enums.ShopCode;
import com.example.common.exception.ShopException;
import com.example.common.utils.HttpUtil;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.*;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.dao.OrderPayMapper;
import org.apache.rocketmq.dao.UserMapper;
import org.apache.rocketmq.dao.UserMoneyLogMapper;
import org.apache.rocketmq.service.UserService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.fastjson.JSON.toJSONString;

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

    @Autowired
    private OrderPayMapper orderPayMapper;

    @Autowired
    private UserMoneyLogMapper userMoneyLogExample;

    @Override
    public ResultMsg createOrder(Order order) {
        //远程调用创建订单的请求
        Map<String, Object> paramMap = JSON.parseObject(JSON.toJSONString(order), HashMap.class);
        JSONObject jsonObject = HttpUtil.post("http://localhost:8181/order/addOrder", paramMap);
        ResultMsg resultMsg = JSON.parseObject(toJSONString(jsonObject), ResultMsg.class);
        if(!resultMsg.checkSuccess()){
            return resultMsg;
        }


        //mq事务消息 扣减库存
        GoodsOrderLog goodsOrderLog = new GoodsOrderLog();
        goodsOrderLog.setOrderId((Long)resultMsg.getData());
        goodsOrderLog.setGoodsId(order.getGoodsId());
        goodsOrderLog.setGoodsNumber(order.getGoodsNumber());
        goodsOrderLog.setLogTime(new Date());
        String msg = toJSONString(goodsOrderLog);
        TransactionSendResult subtractStockTopic = rocketMQTemplate.sendMessageInTransaction("subtractStockTopic", MessageBuilder.withPayload(msg).build(), null);

        //mq事务消息 扣减用户余额
        TransactionSendResult subtractUserMoneyTopic = rocketMQTemplate.sendMessageInTransaction("subtractUserMoneyTopic", MessageBuilder.withPayload(msg).build(), null);

        //向用户发送
        return resultMsg;
    }

    @Override
    public ResultMsg addOrderPay(OrderPay orderPay) {
        //远程调用支付业务请求
        orderPay.setIsPaid(1);
        Map<String, Object> paramMap = JSON.parseObject(JSON.toJSONString(orderPay), Map.class);
        JSONObject post = HttpUtil.post("http://localhost:8184/orderPay/addOrderPay", paramMap);
        ResultMsg resultMsg = JSON.parseObject(toJSONString(post), ResultMsg.class);

        //mq事务消息 扣减用户余额
        UserMoneyLog userMoneyLog = new UserMoneyLog();
        userMoneyLog.setOrderId(orderPay.getOrderId());
        userMoneyLog.setUserId(orderPay.getUserId());
        userMoneyLog.setMoneyLogType(1);
        userMoneyLog.setUseMoney(orderPay.getPayAmount());
        userMoneyLog.setCreateTime(new Date());
        String msg = toJSONString(userMoneyLog);
        TransactionSendResult subtractUserMoneyTopic = rocketMQTemplate.sendMessageInTransaction("subtractUserMoneyTopic", MessageBuilder.withPayload(msg).build(), null);

        //mq事务消息 增加用户积分
        TransactionSendResult addUserScoreTopic = rocketMQTemplate.sendMessageInTransaction("addUserScoreTopic", MessageBuilder.withPayload(msg).build(), null);

        return ResultMsg.success();
    }

    @Override
    public ResultMsg<BigDecimal> getUserMoney(Long userId) {
        User user = baseMapper.selectById(userId);
        return ResultMsg.success(user.getUserMoney());
    }

    @Override
    public ResultMsg addUserScore(Long userId, Integer userScore) {
        baseMapper.addUserScore(userId, userScore);
        return ResultMsg.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultMsg changeUserMoney(UserMoneyLog userMoneyLog) {
        //判断请求参数是否合法
        if (userMoneyLog == null
                || userMoneyLog.getUserId() == null
                || userMoneyLog.getUseMoney() == null
                || userMoneyLog.getOrderId() == null
                || userMoneyLog.getUseMoney().compareTo(BigDecimal.ZERO) <= 0) {
            ShopException.cast(ShopCode.SHOP_REQUEST_PARAMETER_VALID);
        }

        //查询该订单是否存在付款记录
        QueryWrapper orderPayExample = new QueryWrapper<UserMoneyLog>();
        orderPayExample.eq("order_id", userMoneyLog.getOrderId());
        orderPayExample.eq("user_id",userMoneyLog.getUserId());
        int count = userMoneyLogExample.selectCount(orderPayExample);
        //判断余额操作行为
        //【付款操作】
        if (userMoneyLog.getMoneyLogType().equals(ShopCode.SHOP_USER_MONEY_PAID.getCode())) {
            //订单已经付款，则抛异常
            if (count > 0) {
                ShopException.cast(ShopCode.SHOP_ORDER_PAY_STATUS_IS_PAY);
            }
            //用户账户扣减余额
            baseMapper.subUserMoney(userMoneyLog.getUserId(), userMoneyLog.getUseMoney());
        }
        //【退款操作】
        if (userMoneyLog.getMoneyLogType().equals(ShopCode.SHOP_USER_MONEY_REFUND.getCode())) {
            //如果订单未付款,则不能退款,抛异常
            if (count == 0) {
                ShopException.cast(ShopCode.SHOP_ORDER_PAY_STATUS_NO_PAY);
            }
            //防止多次退款
            orderPayExample = new QueryWrapper<UserMoneyLog>();
            orderPayExample.eq("order_id", userMoneyLog.getOrderId());
            orderPayExample.eq("user_id",userMoneyLog.getUserId());
            orderPayExample.eq("money_log_type",ShopCode.SHOP_USER_MONEY_REFUND.getCode());

            count = userMoneyLogExample.selectCount(orderPayExample);
            if (count > 0) {
                ShopException.cast(ShopCode.SHOP_USER_MONEY_REFUND_ALREADY);
            }
            //用户账户添加余额
            baseMapper.addUserMoney(userMoneyLog.getUserId(), userMoneyLog.getUseMoney());
        }


        //记录用户使用余额日志
        userMoneyLog.setCreateTime(new Date());
        userMoneyLogExample.insert(userMoneyLog);
        return ResultMsg.success();
    }
}

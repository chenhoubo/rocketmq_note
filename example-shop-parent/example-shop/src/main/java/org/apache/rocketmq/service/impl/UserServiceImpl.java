package org.apache.rocketmq.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.utils.HttpUtil;
import com.example.common.utils.ResultMsg;
import com.example.domain.po.*;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.dao.UserMapper;
import org.apache.rocketmq.service.UserService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

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
}

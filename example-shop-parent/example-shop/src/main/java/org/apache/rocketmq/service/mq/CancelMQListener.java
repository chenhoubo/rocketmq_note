package org.apache.rocketmq.service.mq;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.enums.ShopCode;
import com.example.domain.po.ConsumerMsg;
import com.example.domain.po.Goods;
import com.example.domain.po.GoodsOrderLog;
import com.example.domain.to.MQEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.dao.ConsumerMsgMapper;
import org.apache.rocketmq.dao.GoodsMapper;
import org.apache.rocketmq.dao.GoodsOrderLogMapper;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/4/27 11:01
 * @Desc 清退库存-监听
 * @since seeingflow
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.order.topic}",
        consumerGroup = "${mq.order.consumer.group.name}",
        messageModel = MessageModel.BROADCASTING)
public class CancelMQListener implements RocketMQListener<MessageExt> {

    @Value("${mq.order.consumer.group.name}")
    private String groupName;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private ConsumerMsgMapper mqConsumerLogMapper;

    @Autowired
    private GoodsOrderLogMapper goodsNumberLogMapper;

    @Override
    public void onMessage(MessageExt messageExt) {
        String msgId=null;
        String tags=null;
        String keys=null;
        String body=null;
        try {
            //1. 解析消息内容
            msgId = messageExt.getMsgId();
            tags= messageExt.getTags();
            keys= messageExt.getKeys();
            body= new String(messageExt.getBody(),"UTF-8");

            log.info("接受消息成功");

            //2. 查询消息消费记录
            QueryWrapper<ConsumerMsg> consumerMsgWrapper = new QueryWrapper<>();
            consumerMsgWrapper.eq("msg_id",msgId)
                    .eq("msg_tag",tags)
                    .eq("msg_key",keys)
                    .eq("group_name", groupName);
            ConsumerMsg mqConsumerLog = mqConsumerLogMapper.selectOne(consumerMsgWrapper);
            if(Objects.nonNull(mqConsumerLog)){
                //3. 判断如果消费过...
                //3.1 获得消息处理状态
                Integer status = mqConsumerLog.getConsumerStatus();
                //处理过...返回
                if(ShopCode.SHOP_MQ_MESSAGE_STATUS_SUCCESS.getCode()==status.intValue()){
                    log.info("消息:"+msgId+",已经处理过");
                    return;
                }

                //正在处理...返回
                if(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode()==status.intValue()){
                    log.info("消息:"+msgId+",正在处理");
                    return;
                }

                //处理失败
                if(ShopCode.SHOP_MQ_MESSAGE_STATUS_FAIL.getCode()==status.intValue()){
                    //获得消息处理次数
                    Integer times = mqConsumerLog.getConsumerTimes();
                    if(times > 3){
                        log.info("消息:"+msgId+",消息处理超过3次,不能再进行处理了");
                        return;
                    }
                    mqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode());

                    //使用数据库乐观锁更新
                    QueryWrapper<ConsumerMsg> example = new QueryWrapper<>();
                    example.eq("msg_id",msgId)
                            .eq("msg_tag",mqConsumerLog.getMsgTag())
                            .eq("msg_key",mqConsumerLog.getMsgKey())
                            .eq("group_name", groupName)
                            .eq("consumer_times", mqConsumerLog.getConsumerTimes());
                    int r = mqConsumerLogMapper.update(mqConsumerLog, example);
                    if(r<=0){
                        //未修改成功,其他线程并发修改
                        log.info("并发修改,稍后处理");
                    }
                }
            }else{
                //4. 判断如果没有消费过...
                mqConsumerLog = new ConsumerMsg();
                mqConsumerLog.setMsgTag(tags);
                mqConsumerLog.setMsgKey(keys);
                mqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_PROCESSING.getCode());
                mqConsumerLog.setMsgBody(body);
                mqConsumerLog.setMsgId(msgId);
                mqConsumerLog.setConsumerTimes(0);

                //将消息处理信息添加到数据库
                mqConsumerLogMapper.insert(mqConsumerLog);
            }
            //5. 回退库存
            MQEntity mqEntity = JSON.parseObject(body, MQEntity.class);
            Long goodsId = mqEntity.getGoodsId();
            Goods goods = goodsMapper.selectById(goodsId);
            goods.setGoodsNumber(goods.getGoodsNumber()+mqEntity.getGoodsNumber());
            goodsMapper.updateById(goods);

            //记录库存操作日志
            GoodsOrderLog goodsNumberLog = new GoodsOrderLog();
            goodsNumberLog.setOrderId(mqEntity.getOrderId());
            goodsNumberLog.setGoodsId(goodsId);
            goodsNumberLog.setGoodsNumber(mqEntity.getGoodsNumber());
            goodsNumberLog.setLogTime(new Date());
            goodsNumberLogMapper.insert(goodsNumberLog);

            //6. 将消息的处理状态改为成功
            mqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_SUCCESS.getCode());
            mqConsumerLog.setConsumerTimestamp(new Date());
            mqConsumerLogMapper.updateById(mqConsumerLog);
            log.info("回退库存成功");
        } catch (Exception e) {
            e.printStackTrace();
            QueryWrapper<ConsumerMsg> consumerMsgWrapper = new QueryWrapper<>();
            consumerMsgWrapper.eq("msg_id",msgId)
                    .eq("msg_tag",tags)
                    .eq("msg_key",keys)
                    .eq("group_name", groupName);
            ConsumerMsg mqConsumerLog = mqConsumerLogMapper.selectOne(consumerMsgWrapper);
            if(mqConsumerLog==null){
                //数据库未有记录
                mqConsumerLog = new ConsumerMsg();
                mqConsumerLog.setMsgTag(tags);
                mqConsumerLog.setMsgKey(keys);
                mqConsumerLog.setConsumerStatus(ShopCode.SHOP_MQ_MESSAGE_STATUS_FAIL.getCode());
                mqConsumerLog.setMsgBody(body);
                mqConsumerLog.setMsgId(msgId);
                mqConsumerLog.setConsumerTimes(1);
                mqConsumerLogMapper.insert(mqConsumerLog);
            }else{
                mqConsumerLog.setConsumerTimes(mqConsumerLog.getConsumerTimes()+1);
                mqConsumerLogMapper.updateById(mqConsumerLog);
            }
        }
    }
}
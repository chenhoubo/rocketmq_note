package org.apache.rocketmq.houbo.type;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;

import java.util.List;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/2/4 16:21
 * @Desc 消费者
 * @since rocketmqTest
 */
public class Consumer {
    //同组内的消费者负载均衡消息
    //不同组内的消费者广播消费所有的消息
    public static void main(String[] args) throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("group2");
        consumer.setNamesrvAddr("localhost:9876");
        //设置消费模式
        consumer.setMessageModel(MessageModel.CLUSTERING);//默认为负载均衡模式
//        consumer.setMessageModel(MessageModel.BROADCASTING);//设置为广播模式
        consumer.subscribe("topic1","*");
        consumer.registerMessageListener(new MessageListenerConcurrently(){
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                for (MessageExt messageExt : list) {
                    System.out.println(messageExt);
                    byte[] body = messageExt.getBody();
                    System.out.println(new String(body));
                    System.out.println("===============================================");
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        System.out.println("消费者跑起来了！");
    }
}

package org.apache.rocketmq.houbo.simple;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/2/4 16:21
 * @Desc 消费者
 * @since rocketmqTest
 */
public class Consumer {
    public static void main(String[] args) throws Exception {
        //1.谁来收？
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("group1");
        //2.从哪里收消息
        consumer.setNamesrvAddr("localhost:9876");
        //3.监听哪个消息队列
        consumer.subscribe("topic1","*");
        //4.处理业务流程,注册一个监听器
        consumer.registerMessageListener(new MessageListenerConcurrently(){
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                //写我们的业务逻辑
                for (MessageExt messageExt : list) {
                    System.out.println(messageExt);
                    byte[] body = messageExt.getBody();
                    System.out.println(new String(body));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        System.out.println("消费者跑起来了！");
        //千万别关消费者，因为是一个监听器，保持长连接
    }
}

package org.apache.rocketmq.houbo.simple;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/2/4 15:55
 * @Desc 发送消息
 * @since rocketmqTest
 */
public class Producer {
    public static void main(String[] args) throws Exception {
        //1.谁来发？
        DefaultMQProducer producer = new DefaultMQProducer("group1");
        //2.发给谁？
        producer.setNamesrvAddr("localhost:9876");//nameServer地址
        producer.start();
        //3.怎么发？
        //4.发什么？
        String msg="hello world, 陈厚伯";
        Message message = new Message("topic1","tag1",msg.getBytes());
        SendResult sendResult = producer.send(message);//这里是同步消息。
        //5.发的结果是什么？
        System.out.println(sendResult);
        //6.打扫战场
        producer.shutdown();
    }

}

package org.apache.rocketmq.houbo.filter;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/2/4 15:55
 * @Desc 生产者-消息过滤
 * @since rocketmqTest
 */
public class Producer {
    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("group1");
        producer.setNamesrvAddr("localhost:9876");//nameServer地址
        producer.start();
        String msg="hello world, 陈厚伯";
        Message message = new Message("topic1","tag1",msg.getBytes());
        //消息追加属性
        message.putUserProperty("name","zhangsan");
        message.putUserProperty("age","18");
        SendResult sendResult = producer.send(message);//这里是同步消息。
        System.out.println(sendResult);
        producer.shutdown();
    }

}

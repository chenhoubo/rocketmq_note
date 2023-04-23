package org.apache.rocketmq.houbo.type;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.util.ArrayList;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/2/6 15:57
 * @Desc 批量消息
 * @since seeingflow
 */
public class ProducerBatch {
    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("group1");
        producer.setNamesrvAddr("localhost:9876");//nameServer地址
        producer.start();
        ArrayList<Message> msgList = new ArrayList<>();
        String msg1 = "hello world, 批量消息，陈厚伯";
        Message message1 = new Message("topic1", "tag1", msg1.getBytes());
        msgList.add(message1);
        String msg2 = "hello world, 批量消息，陈厚伯";
        Message message2 = new Message("topic1", "tag1", msg2.getBytes());
        msgList.add(message2);
        String msg3 = "hello world, 批量消息，陈厚伯";
        Message message3 = new Message("topic1", "tag1", msg3.getBytes());
        msgList.add(message3);
        SendResult sendResult = producer.send(msgList);
        System.out.println(sendResult);
//        producer.shutdown();
    }
}

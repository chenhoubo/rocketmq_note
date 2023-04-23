package org.apache.rocketmq.houbo.one2many;

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
        DefaultMQProducer producer = new DefaultMQProducer("testGroup");
        producer.setNamesrvAddr("localhost:9876");//nameServer地址
        producer.start();
        for (int i = 0; i < 10; i++) {
            String msg="hello world, 陈厚伯" + i;
            Message message = new Message("testTopic","testTag", msg.getBytes());
            SendResult sendResult = producer.send(message);//这里是同步消息。
            System.out.println(sendResult);
        }
//        producer.shutdown();
    }

}

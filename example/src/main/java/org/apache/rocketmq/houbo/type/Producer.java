package org.apache.rocketmq.houbo.type;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/2/4 15:55
 * @Desc 发送消息---同步消息、异步消息、单向消息
 * @since rocketmqTest
 */
public class Producer {
    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("group2");
        producer.setNamesrvAddr("localhost:9876");//nameServer地址
        //启动producer
        producer.start();

        //同步消息。
//        for (int i = 0; i < 10; i++) {
//            String msg="hello world, 陈厚伯" + i;
//            Message message = new Message("topic1","tag1",msg.getBytes());
//            SendResult sendResult = producer.send(message);
//            System.out.println(sendResult);
//        }

        for (int i = 0; i < 10; i++) {
            String msg = "hello world, 陈厚伯" + i;
            Message message = new Message("topic1","*",msg.getBytes());
            //异步消息
//            producer.send(message, new SendCallback() {
//                //发送成功的回调方法
//                @Override
//                public void onSuccess(SendResult sendResult) {
//                    System.out.println(sendResult);
//                }
//                //发送失败的回调方法
//                @Override
//                public void onException(Throwable throwable) {
//                    System.out.println(throwable);
//                }
//            });
//            System.out.println("异步发送完成");

            //单向消息
//            producer.sendOneway(message);

            //延时消息
            //"1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h"
            message.setDelayTimeLevel(1);
            SendResult sendResult = producer.send(message);
            System.out.println(sendResult);
        }
        //这里不能先关闭--需要等待回调是否成功
//        producer.shutdown();
    }

}

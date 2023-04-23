package org.apache.rocketmq.houbo.transaction;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/2/4 15:55
 * @Desc 发送事务消息
 * @since rocketmqTest
 */
public class Producer {
    public static void main(String[] args) throws Exception {
        //事务消息生产者
        TransactionMQProducer producer = new TransactionMQProducer("group1");
        producer.setNamesrvAddr("localhost:9876");//nameServer地址
        //设置事务的监听
        producer.setTransactionListener(new TransactionListener() {
            //正常事务过程
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {
                System.out.println("执行正常事务过程");
                //把消息保存到数据库中
                //mysql : sql insert，，，，，根据本地事务状态返回事务状态
//                return LocalTransactionState.COMMIT_MESSAGE;//提交消息
//                return LocalTransactionState.ROLLBACK_MESSAGE;//回滚消息
                return LocalTransactionState.UNKNOW;//未知消息,进入事务补偿过程
            }
            //事务补偿过程
            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
                System.out.println("进入事务补偿过程");
                //mysql : sql insert，，，，，根据本地事务状态返回事务状态
                return LocalTransactionState.COMMIT_MESSAGE;//提交消息
//                return LocalTransactionState.ROLLBACK_MESSAGE;//回滚消息
//                return LocalTransactionState.UNKNOW;//未知消息,进入事务补偿过程
            }
        });
        producer.start();
        String msg="hello world,TransactionListener, 陈厚伯";
        Message message = new Message("topic13","tag1",msg.getBytes());
        //发送正常的事务消息
        TransactionSendResult sendResult = producer.sendMessageInTransaction(message, null);
        System.out.println(sendResult);
        //不关生产者
//        producer.shutdown();
    }

}

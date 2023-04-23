package org.apache.rocketmq.houbo.sort;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/2/4 15:55
 * @Desc 发送消息-顺序消息
 * @since rocketmqTest
 */
public class Producer {
    public static void main(String[] args) throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer("group1");
        producer.setNamesrvAddr("localhost:9876");//nameServer地址
        producer.start();
        //模拟订单的数据
        List<OrderStep> orderList = new ArrayList<>();

        OrderStep orderDemo = new OrderStep();
        orderDemo.setOrderId(1L);
        orderDemo.setDesc("创建");
        orderList.add(orderDemo);

        orderDemo = new OrderStep();
        orderDemo.setOrderId(2L);
        orderDemo.setDesc("创建");
        orderList.add(orderDemo);

        orderDemo = new OrderStep();
        orderDemo.setOrderId(1L);
        orderDemo.setDesc("付款");
        orderList.add(orderDemo);

        orderDemo = new OrderStep();
        orderDemo.setOrderId(3L);
        orderDemo.setDesc("创建");
        orderList.add(orderDemo);

        orderDemo = new OrderStep();
        orderDemo.setOrderId(2L);
        orderDemo.setDesc("付款");
        orderList.add(orderDemo);

        orderDemo = new OrderStep();
        orderDemo.setOrderId(3L);
        orderDemo.setDesc("付款");
        orderList.add(orderDemo);

        orderDemo = new OrderStep();
        orderDemo.setOrderId(2L);
        orderDemo.setDesc("完成");
        orderList.add(orderDemo);

        orderDemo = new OrderStep();
        orderDemo.setOrderId(1L);
        orderDemo.setDesc("推送");
        orderList.add(orderDemo);

        orderDemo = new OrderStep();
        orderDemo.setOrderId(3L);
        orderDemo.setDesc("完成");
        orderList.add(orderDemo);

        orderDemo = new OrderStep();
        orderDemo.setOrderId(1L);
        orderDemo.setDesc("完成");
        orderList.add(orderDemo);

        //错误发送
//        for (OrderStep orderStep : orderList) {
//            Message message = new Message("topic11", "tag1", orderStep.toString().getBytes());
//            SendResult send = producer.send(message);
//            System.out.println(send);
//        }

        //正确的发送，队列选择器
        for (OrderStep orderStep : orderList) {
            Message message = new Message("topic11", "tag1", orderStep.toString().getBytes());
            producer.send(message, new MessageQueueSelector() {
                //队列选择的方法
                @Override
                public MessageQueue select(List<MessageQueue> list, Message message, Object o) {
                    //orderId对应一个确定的队列
                    //队列数
                    int size = list.size();
                    //取模
                    int orderId = (int)orderStep.getOrderId();
                    int i = orderId%size;//0,1,2,3
                    //取出确定的队列
                    return list.get(i);
                }
            },null);
        }
    }

}

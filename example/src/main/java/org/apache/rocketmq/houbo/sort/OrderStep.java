package org.apache.rocketmq.houbo.sort;

import java.io.Serializable;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/2/7 15:22
 * @Desc 订单步骤的实体类
 * @since seeingflow
 */
public class OrderStep implements Serializable {
    private long orderId;
    private String desc;

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "OrderStep{" +
                "orderId=" + orderId +
                ", desc='" + desc + '\'' +
                '}';
    }

}

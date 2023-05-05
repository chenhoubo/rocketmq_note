package org.apache.rocketmq.test.atomic;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Chenhoubo
 * @version v1.0
 * @date 2023/5/5 11:26
 * @Desc
 * @since seeingflow
 */
public class AtomicIntegerTest {
    public static void main(String[] args) {
        AtomicInteger atomicInteger = new AtomicInteger();
        System.out.println(atomicInteger.incrementAndGet());
        System.out.println(atomicInteger.incrementAndGet());
        System.out.println(atomicInteger);
    }
}

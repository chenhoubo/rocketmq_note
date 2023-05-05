package org.apache.rocketmq.test.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * synchronized和Lock区别：
 *
 * synchronized 是java关键字，Lock只是java中的一个类。
 * synchronized 无法感知锁的状态，但是Lock可以判断是否成功拿到锁。
 * synchronized 自动加锁和释放锁，但Lock必须手动。
 * 如果拿到synchronized的线程阻塞，其他线程会继续等待；但Lock可能不等待(lock.tryLock())。
 * synchronized 可重入锁、不可中断、不公平；
 * Lock 可重入锁，可以判断是否中断、可以选择公平锁/非公平锁。
 * synchronized 适合于少量代码同步问题；Lock适合大量的同步代码。
 */
public class SaleTicketTest {
    public static void main(String[] args) {
        // lambda 表达式 ()表示run() 其中"()"中可以写明参数 ，->{} 表示 run后的代码块{}
        // 支持 lambda 表达式，必须是接口上有 @FunctionalInterface 注解
        // @FunctionalInterface 注解的接口，只能存在一个 抽象方法！

        Ticket3 ticket3 = new Ticket3();
        new Thread(()->{
            for (int i = 0; i < 40; i++) {
                ticket3.sale();
            }
        },"A").start();
        new Thread(()->{
            for (int i = 0; i < 40; i++) {
                ticket3.sale();
            }
        },"B").start();
        new Thread(()->{
            for (int i = 0; i < 40; i++) {
                ticket3.sale();
            }
        },"C").start();

    }
}

class Ticket3{
    private Integer num = 40;//目标数
    private Integer saleNum = 0; //卖出数

    // 可重入锁(最实用)
    // 调用无参构造，生成 “不公平锁”
    // 可以传递参数 ReentrantLock(boolean fair) {sync = fair ? new FairSync() : new NonfairSync();} true为公平锁
    // 不公平锁，性能更好，可以插队！
    Lock lock = new ReentrantLock();
    public void sale(){
        lock.lock(); //加锁
        //lock.tryLock();
        try {
            if(num > 0){
                num --;
                saleNum ++;
                System.out.println(Thread.currentThread().getName()+"卖票，共卖了"+saleNum+"张，剩余："+num+"张");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();//释放锁
        }
    }
}
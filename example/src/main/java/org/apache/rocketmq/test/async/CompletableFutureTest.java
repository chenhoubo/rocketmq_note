package org.apache.rocketmq.test.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Chenhoubo
 * @date 2023/5/10 14:58
 * @Desc CompletableFuture function test
 */
public class CompletableFutureTest {


    public static void main(String[] args) throws Exception {
//        testThenCombine();
        testThenAcceptAsync();
        Thread.sleep(10000);
    }

    public static void testThenCombine() throws ExecutionException, InterruptedException {
        // 异步操作
        CompletableFuture<String> resultFuture = CompletableFuture.supplyAsync(
                () -> {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("resultFuture---执行");
//                    throw new RuntimeException();
                    return "resultFuture执行完了";
                });
        CompletableFuture<Integer> resultFuture2 = CompletableFuture.supplyAsync(()->{
            System.out.println("resultFuture2---执行");
//            throw new RuntimeException();
            return 200;
        });

        //两个都执行成功才会执行的
        CompletableFuture<String> stringCompletableFuture = resultFuture.thenCombine(resultFuture2, (flushStatus, replicaStatus) -> {
            System.out.println("flushStatus:" + flushStatus);
            System.out.println("replicaStatus:" + replicaStatus);
            return "resultFuture 和 resultFuture2 都执行完了";
        });
        String s = stringCompletableFuture.get();
        System.out.println("执行结果：" + s);
    }

    public static void testThenAcceptAsync() throws ExecutionException, InterruptedException {
        // 异步操作
        CompletableFuture<String> resultFuture = CompletableFuture.supplyAsync(
                () -> {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("resultFuture---执行");
//                    throw new RuntimeException();
                    return "resultFuture执行完了";
                });

        ExecutorService executor = Executors.newCachedThreadPool();

        //两个都执行成功才会执行的
        CompletableFuture<Void> stringCompletableFuture = resultFuture.thenAcceptAsync(result -> {
            System.out.println("result:" + result);
            System.out.println("执行成功" );
            String res = "执行成功";
        },executor);
    }

}

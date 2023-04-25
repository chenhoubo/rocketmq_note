package org.apache.rocketmq.houbo.mmap;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/**
 * @author Chenhoubo
 * @date 2023/4/25 10:24
 * @Desc  transferTo  零拷贝
 */
public class NewIOClient {
    public static void main(String[] args) throws Exception {
        //socket套接字
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost",8081));
        socketChannel.configureBlocking(true);
        //文件
        String fileName = "/Users/chenhoubo/Desktop/file/mq/houbo.jpeg";
        //FileChannel 文件读写、映射和操作的通道
        FileChannel fileChannel = new FileInputStream(fileName).getChannel();
        long startTime = System.currentTimeMillis();
        //transferTo方法用到了零拷贝，底层是sendfile，这里只需要发生两次copy和两次上下文切换
        long transferCount = fileChannel.transferTo(0, fileChannel.size(), socketChannel);
        long endTime = System.currentTimeMillis();
        System.out.println("发送总字节数：" + transferCount + " 耗时：" + (endTime - startTime) + "ms");
        //释放资源
        fileChannel.close();
        socketChannel.close();
    }
}

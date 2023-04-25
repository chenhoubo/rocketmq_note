package org.apache.rocketmq.houbo.mmap;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.net.Socket;

/**
 * @author Chenhoubo
 * @date 2023/4/25 10:04
 * @Desc 传统读写+网络发送：客户端 4次copy
 */
public class TranditionClient {
    public static void main(String[] args) throws Exception {
        //创建socket对象
        Socket socket = new Socket("localhost", 8081);
        //创建磁盘文件 /Users/chenhoubo/Desktop/file/mq/houbo.jpeg
        String fileName = "/Users/chenhoubo/Desktop/file/mq/houbo.jpeg";
        //创建输入流对象
        FileInputStream inputStream = new FileInputStream(fileName);
        //创建输出流
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        //创建字节数组--引用层缓冲区
        byte[] buffer = new byte[1024];
        long readCount = 0;
        long total = 0;
        long startTime = System.currentTimeMillis();
        //这里要发生两次copy和两次上下文切换
        while((readCount = inputStream.read(buffer)) > 0){
            total += readCount;
            //网络发送：这里要发生两次copy和两次上下文切换
            dataOutputStream.write(buffer);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("发送总字节数：" + total + ", 耗时：" + (endTime - startTime) + "ms");
        //释放资源
        dataOutputStream.close();
        socket.close();
        inputStream.close();
    }
}

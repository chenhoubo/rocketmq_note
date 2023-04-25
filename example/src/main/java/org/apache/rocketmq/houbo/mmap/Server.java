package org.apache.rocketmq.houbo.mmap;

import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Chenhoubo
 * @date 2023/4/25 09:54
 * @Desc 服务端
 */
public class Server {

    public static void main(String[] args) throws Exception {
        //创建serversocket 对象--8081服务
        ServerSocket serverSocket = new ServerSocket(8081);
        //循环监听连接
        while(true){
            Socket socket = serverSocket.accept();//客户端发起网络请求---连接
            //创建输入流对象
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            int byteCount = 0;
            try {
                byte[] bytes = new byte[1024];//创建缓冲区字节数组
                while(true){
                    int readCount = dataInputStream.read(bytes,0,bytes.length);
                    byteCount = byteCount + readCount;
                    if(readCount == -1){
                        System.out.println("服务端接受：" + byteCount + "字节");
                        break;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}

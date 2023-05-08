package org.apache.rocketmq.houbo.mmap;

import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Chenhoubo
 * @date 2023/4/24 18:17
 * @Desc 零拷贝
 */
public class MmapCopy {
    public static String path = "/Users/chenhoubo/Desktop/file/mq";//需要内存映射的文件目录
    public static void main(String[] args) throws Exception {
        //映射的文件
        File file1 = new File(path,"00000000000000000000");
        //映射文件的fileChannel对象（文件通道）
        FileChannel fileChannel = new RandomAccessFile(file1, "rw").getChannel();
        //MappedByteBuffer （零拷贝之内存映射：mmap）
        //FileChannel配合着ByteBuffer，将读写的数据缓存到内存中(操纵大文件时可以显著提高效率)
        //fileChannel定义了map方法，MMAP的映射，它可以把一个文件从 position 位置开始的 size 大小的区域映射为内存映像文件
        MappedByteBuffer mmap = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 1024);
        //向mmap put方法，模拟MQ写入数据
        mmap.put("chen".getBytes());
        //刷新写入磁盘（刷盘）
        mmap.flip();
        byte[] bb = new byte[4];
        //从mmap中读取文件，模拟MQ消费
        mmap.get(bb,0,4);
        System.out.println(new String(bb));
        //解除MMAP 内存映射
        unmmap(mmap);
    }
    //最终释放，显示调用System.gc()方法，还是GC
    private static void unmmap(MappedByteBuffer bb){
        Cleaner cl = ((DirectBuffer) bb).cleaner();
        if(cl != null) {
            cl.clean();
        }
    }
}

package com.holysu.nio.demo;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * nio 服务端
 */
public class NioServer {

    private static Selector selector;
    private static LinkedBlockingQueue<SelectionKey> requestQueue;
    private static ExecutorService threadPool;

    public static void main(String[] args) {
        init();
        listen();
    }

    /**
     * 初始化
     */
    private static void init() {
        ServerSocketChannel serverSocketChannel = null;
        try {
            selector = Selector.open();

            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false); // 设置为非阻塞

            // 绑定端口
            serverSocketChannel.socket().bind(new InetSocketAddress(9000), 100);

            // 监听连接请求
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }
        requestQueue = new LinkedBlockingQueue<SelectionKey>(500);
        threadPool = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            threadPool.execute(new Worker());
        }
    }

    /**
     * 监听客户端请求
     */
    private static void listen() {
        while (true) {
            try {
                selector.select();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove(); //！！！ 很重要的一步，如果不把处理完的已就绪 channel， remove 掉，下次 selector.select() 的时候回又重复处理
                    requestQueue.offer(key);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    static class Worker implements Runnable {

        public void run() {
            while (true) {

                try {
                    SelectionKey key = requestQueue.take();
                    handleRequest(key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleRequest(SelectionKey key) throws IOException, ClosedChannelException {
            SocketChannel channel = null;

            try {
                if (key.isAcceptable()) {
                    System.out.println(new Date() + "[" + Thread.currentThread().getName() + "]接收到连接......");
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

                    // 通过 serverSocketChannel 接受客户端连接
                    channel = serverSocketChannel.accept();
                    System.out.println(new Date() + "[" + Thread.currentThread().getName() + "]建立连接时候获取到的 channel=" + channel);

                    channel.configureBlocking(false); // 设为非阻塞
                    channel.register(selector, SelectionKey.OP_READ);

                } else if (key.isReadable()) {
                    channel = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    int count = channel.read(byteBuffer); // 从channel读数据到 buffer 中， position 会相应的向前推进

                    System.out.println("[" + Thread.currentThread().getName() + "] 接收到请求......");

                    if (count > 0) {
                        byteBuffer.flip(); // flip 会设置 limit， 一般在 channel.read 后 再次 get、write之前调用

                        System.out.println(new Date() + "服务端接收到请求： \"" + new String(byteBuffer.array(), 0, count) + "\"");

                        // 读取完请求后， 关注可先事件 准备输出响应
                        channel.register(selector, SelectionKey.OP_WRITE);
                    }
                } else if (key.isWritable()) {

//                    System.out.println("准备发送响应......");
                    // 坚挺到 writable 即可向客户端输出响应了
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    buffer.put("收到".getBytes());
                    buffer.flip();
// 试试用这种可以不
//     ByteBuffer byteBuffer = ByteBuffer.wrap("收到".getBytes());
//     byteBuffer.flip();

                    channel = (SocketChannel) key.channel();
                    channel.write(buffer);
                    channel.register(selector, SelectionKey.OP_READ); // 再次监听可读事件，等待客户端再次从这个链接发送数据过来
                }
            } catch (Throwable t) {
                t.printStackTrace();

                if (channel != null) {
                    channel.close();
                }
            }

        }

    }
}

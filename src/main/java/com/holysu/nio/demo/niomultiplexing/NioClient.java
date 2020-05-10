package com.holysu.nio.demo.niomultiplexing;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * nio 客户端
 */
public class NioClient {
    public static void main(String[] args) {
        for (int i = 0; i < 1; i++) {
            new Worker().start();
        }
    }

    static class Worker extends Thread {
        @Override
        public void run() {
            SocketChannel channel = null;
            Selector selector = null;

            try {
                channel = SocketChannel.open();
                channel.configureBlocking(false); // 设置为非阻塞模式
                channel.connect(new InetSocketAddress("localhost", 9000)); // 发起连接，非阻塞模式下， 连接发起不会阻塞等待到连接完成

                selector = Selector.open();
                channel.register(selector, SelectionKey.OP_CONNECT); // 先监听 connect 连接行为

                while (true) {
                    selector.select(); // 阻塞等待事件发生

                    Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();
                    while (keysIterator.hasNext()) {
                        SelectionKey key = keysIterator.next();
                        keysIterator.remove(); //！！！ 很重要的一步
                        if (key.isConnectable()) { // server 端返回一个 connectable 的消息
                            channel = (SocketChannel) key.channel();
                            // 阻塞到tcp连接建立完毕
                            channel.finishConnect();

                            System.out.println( Thread.currentThread().getName() + " 连接建立后首次发送请求......");
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            buffer.put("你好".getBytes());
                            buffer.flip();
                            channel.write(buffer);

                            channel.register(selector, SelectionKey.OP_READ); // ！！！ 每次都要重新监听事件才可以
                        } else if (key.isReadable()) { // isReadable() 表示收到服务端发来的信息了，可以读取了
                            System.out.println("收到服务端的响应...");
                            channel = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            int len = channel.read(buffer); // 从 channel中读取 server端发来的数据
                            if (len > 0) {
                                buffer.flip();
                                System.out.println(Thread.currentThread().getName() + ": 收到响应: "
                                        + new String(buffer.array(), 0, len));
                                Thread.sleep(1000);
                                channel.register(selector, SelectionKey.OP_WRITE);
                            }
                        } else if (key.isWritable()) {
                            System.out.println(Thread.currentThread().getName() + ": 准备再次发送请求...");
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            buffer.put("hello yayyaya".getBytes());
                            buffer.flip();

                            channel = (SocketChannel) key.channel();
                            channel.write(buffer);
                            channel.register(selector, SelectionKey.OP_READ); // 发送完数据后， 监听 read 事件等待服务端响应
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (selector != null) {
                    try {
                        selector.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
}

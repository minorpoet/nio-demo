package com.holysu.nio.demo.niomultiplexing;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * nio 多路复用 server端
 */
public class NioServer {

    private static Selector selector = null;

    private static ServerSocketChannel serverSocketChannel = null;
    /**
     * 请求队列
     */
    private static LinkedBlockingQueue<SelectionKey> pendingQueue = new LinkedBlockingQueue<>(1000);

    public static void main(String[] args) throws IOException {
        init();
        listen();
    }

    private static void init() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 9000);
        serverSocketChannel.socket().bind(address);
        serverSocketChannel.configureBlocking(false);
        // 监听连接
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

//        kickoff();
    }

    private static void listen() {
        System.out.println("server listen");
        while (true) {
            try {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
//                    pendingQueue.put(key);
                    handleRequest(key);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void kickoff() {
        System.out.println("kickoff work threads");
        for (int i = 0; i < 2; i++) {
            new Thread(new Worker()).start();
        }
    }

    private static class Worker implements Runnable {

        @Override
        public void run() {
            while (true) {
                SelectionKey key = null;
                try {
                    // 用selectionKey做队列处理 貌似有些问题
                    key = pendingQueue.take();
                    handleRequest(key);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                    if (key != null) {
                        try {
                            ((SocketChannel) key.channel()).socket().close();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static void handleRequest(SelectionKey key) throws IOException {
        SocketChannel channel = null;
        if ((key.readyOps() & SelectionKey.OP_ACCEPT) != 0) {
            channel = ((ServerSocketChannel) key.channel()).accept();
            if (channel != null) {
                channel.configureBlocking(false);

                System.out.println(Thread.currentThread().getName() + ": 和客户端建立连接");

                // 建立完连接后，开始监听读事件 接收客户端发来的消息
                // 客户端对应的channel，第一次注册到selector
                channel.register(selector, SelectionKey.OP_READ);
            }
        } else if ((key.readyOps() & SelectionKey.OP_READ) != 0) {
            System.out.println(Thread.currentThread().getName() + ": 读取到客户端发来的请求");
            channel = (SocketChannel) key.channel();
            InetAddress clientAddress = ((InetSocketAddress) channel.getRemoteAddress()).getAddress();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int count = channel.read(byteBuffer);
            if (count > 0) {
                byteBuffer.flip();
                System.out.println(Thread.currentThread().getName()
                        + ": 接收到客户端请求：" + new String(byteBuffer.array(), 0, count, StandardCharsets.UTF_8));
            }
            // 读取完客户端发来的消息，监听写事件
            key.interestOps(SelectionKey.OP_WRITE);
        } else if ((key.readyOps() & SelectionKey.OP_WRITE) != 0) {
            System.out.println(Thread.currentThread().getName() + ": 向客户端发送响应");
            channel = (SocketChannel) key.channel();
            String response = "收到-";
            ByteBuffer byteBuffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
            channel.write(byteBuffer);
            // 向客户端发送响应后，监听读事件
            key.interestOps(SelectionKey.OP_READ);
        }
    }
}

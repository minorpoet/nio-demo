package com.holysu.nio.demo;


import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * 多线程socket bio网络通信模式 server
 */
public class MultiThreadSocketBIOServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        SocketAddress address = new InetSocketAddress("127.0.0.1", 9527);
        serverSocket.bind(address);

        while (true) {
            final Socket socket = serverSocket.accept();
            // 和客户端建立好连接之后，将对应的 socket 交给一个线程专门处理
            // 对于每个连接，处理线程只能在网络io读取后才能进入处理逻辑
            // 而且如果连接上来多客户端越来越多，服务端的线程数就会不断暴涨，
            // server端会被大量线程耗尽资源，这个时候cpu将疲于应对上下文切换 线程将获取不到时间片进行处理
            new Thread(() -> {
                try {
                    InputStream inputStream = socket.getInputStream();
                    while (true) {
                        DataInputStream dataInputStream = new DataInputStream(inputStream);
                        int len = dataInputStream.readInt();
                        byte[] data = new byte[len];
                        dataInputStream.read(data);
                        System.out.println(
                                Thread.currentThread().getName()
                                        + " read from client-" + socket.getPort() + ", "
                                        + " content: " + new String(data, "utf-8"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}

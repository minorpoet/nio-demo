package com.holysu.nio.demo.biomultithread;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Random;

/**
 * bio网络通信模式 client
 */
public class MultiThreadSocketBIOClient {
    static Random random = new Random();

    public static void main(String[] args) throws IOException, InterruptedException {
        String serverAddr = "127.0.0.1";
        int serverPort = 9527;
        for (int i = 0; i < 10; i++) {
            Socket socket = new Socket();
            try {
                InetSocketAddress address = new InetSocketAddress(serverAddr, serverPort);
                socket.setTcpNoDelay(true);
                socket.setKeepAlive(true);
                socket.connect(address);
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                String content = "first msg: hello world" + random.nextInt();
                dataOutputStream.writeInt(content.getBytes().length);
                dataOutputStream.write(content.getBytes(Charset.forName("utf-8")));
                dataOutputStream.flush();

                String secondContent = "second msg: " + random.nextInt(99999999);
                dataOutputStream.writeInt(secondContent.getBytes().length);
                dataOutputStream.write(secondContent.getBytes());
                dataOutputStream.flush();
            } catch (Exception e) {
                socket.close();
                e.printStackTrace();
            }
        }

        Thread.sleep(Integer.MAX_VALUE);
    }
}

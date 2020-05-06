package com.holysu.nio.demo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * channel是线程安全的
 */
public class ThreadSafeChannelTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        FileOutputStream fileOutputStream = new FileOutputStream("C:\\tmp\\hello2.txt", true);
        FileChannel fileChannel = fileOutputStream.getChannel();

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    fileChannel.write(ByteBuffer.wrap("hello world;".getBytes()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        Thread.sleep(3000);
        fileChannel.close();
        fileOutputStream.close();
    }
}

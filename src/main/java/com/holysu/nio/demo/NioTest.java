package com.holysu.nio.demo;

import java.nio.ByteBuffer;

public class NioTest {

    public static void main(String[] args) {
        byte[] data = new byte[]{55, 56, 57, 58, 59};

        // wrap 会基于 data 分配 data大小的capacity， position=0，并 limit = position
        ByteBuffer buffer = ByteBuffer.wrap(data);

        printBuffer(buffer);

        buffer = ByteBuffer.wrap(data, 1,2);
        printBuffer(buffer);

        System.out.println(buffer.get()); // 把当前position所在位置的数据读取一位出来
        System.out.println(buffer.position());
        buffer.mark(); // 在position = 1的时候打的mark，标记

//		buffer.position(3);
//		buffer.limit(4);

        System.out.println("set position 2");
        buffer.position(2);
        System.out.println("get(): " + buffer.get());
        System.out.println(buffer.position());

        buffer.reset();
        System.out.println(buffer.position());

        System.out.println("-----------------------------------");
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        System.out.println("limit after allocate: " + byteBuffer.limit());
    }

    private static void printBuffer(ByteBuffer buffer) {
        System.out.println("<<--------------------------------------------");
        System.out.println("capacity: " + buffer.capacity());
        System.out.println("current position: " + buffer.position());
        System.out.println("current limit:" + buffer.limit());
        System.out.println("-------------------------------------------->>");
    }
}

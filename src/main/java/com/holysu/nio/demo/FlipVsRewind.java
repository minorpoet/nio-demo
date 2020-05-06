package com.holysu.nio.demo;

import java.nio.ByteBuffer;

public class FlipVsRewind {

    public static void main(String[] args) {
        byte[] data = new byte[]{55, 56, 57, 58, 59};

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        printBuffer(buffer);

        buffer.putInt(4);
        printBuffer(buffer); // limit = 1024

        //1. flip:  limit = position
        //通常用于将数据读或put到buffer之后，flip() 准备给后续的 write
        //   * buf.put(magic);    // Prepend header
        //   * in.read(buf);      // Read data into rest of buffer
        //   * buf.flip();        // Flip buffer
        //   * out.write(buf);    // Write header + data to channel
        buffer.flip();// limit = 4， flip()之后 limit就设置到了正确位置，后续write的话就只会输出有效的数据部分
        printBuffer(buffer);


        //2. rewind : 不设置limit，假设limit已经在正确的位置了
        // 一般用于在 write 之后 rewind() 可以重新从buffer中读取数据
        //   * out.write(buf);    // Write remaining data
        //   * buf.rewind();      // Rewind buffer
        //   * buf.get(array);    // Copy data into array

    }

    private static void printBuffer(ByteBuffer buffer) {
        System.out.println("<<--------------------------------------------");
        System.out.println("capacity: " + buffer.capacity());
        System.out.println("current position: " + buffer.position());
        System.out.println("current limit:" + buffer.limit());
        System.out.println("-------------------------------------------->>");
    }
}

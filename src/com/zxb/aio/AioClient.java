package com.zxb.aio;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * AIO Client class
 *
 * @author Mr.zxb
 * @date 2019-12-23 15:16
 */
public class AioClient {

    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 8000;

    private AsynchronousSocketChannel socketChannel;

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        try {
            socketChannel = AsynchronousSocketChannel.open();
            Future<Void> future = socketChannel.connect(new InetSocketAddress(DEFAULT_HOST, DEFAULT_PORT));
            future.get();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            for (; ; ) {
                String input = reader.readLine();

                byte[] bytes = input.getBytes();
                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                Future<Integer> integerFuture = socketChannel.write(byteBuffer);
                integerFuture.get();
                byteBuffer.flip();

                Future<Integer> read = socketChannel.read(byteBuffer);
                read.get();

                String s = new String(byteBuffer.array());
                byteBuffer.clear();
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            close(socketChannel);
        }
    }

    public static void main(String[] args) {
        new AioClient().start();
    }

}

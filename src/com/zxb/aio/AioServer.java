package com.zxb.aio;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

/**
 * AIO Server class
 *
 * @author Mr.zxb
 * @date 2019-12-22 16:47
 */
public class AioServer {

    public static final int DEFAULT_PORT = 8000;

    private AsynchronousServerSocketChannel serverSocketChannel;

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
        // 绑定监听端口
        try {
            serverSocketChannel = AsynchronousServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(DEFAULT_PORT));
            System.out.println("启动AIO服务器，监听端口：" + DEFAULT_PORT);

            for (; ; ) {
                serverSocketChannel.accept("", new CompletionHandler<>() {
                    @Override
                    public void completed(AsynchronousSocketChannel result, String attachment) {
                        if (serverSocketChannel.isOpen()) {
                            serverSocketChannel.accept(null, this);
                        }
                        if (result != null && result.isOpen()) {
                            ClientHandler handler = new ClientHandler(result);
                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                            Map<String, Object> of = new HashMap<>();
                            of.put("type", "read");
                            of.put("buffer", byteBuffer);

                            result.read(byteBuffer, of, handler);
                        }
                    }

                    @Override
                    public void failed(Throwable exc, String attachment) {
                        // 处理错误
                        System.out.println(exc.getMessage());
                    }
                });
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(serverSocketChannel);
        }
    }

    public static void main(String[] args) {
        new AioServer().start();
    }
}

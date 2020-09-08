package com.zxb.aio.chatroom;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2019-12-23 17:32
 */
public class AioChatServer {

    public static final int DEFAULT_PORT = 8888;
    public static final String EXIT = "exit";
    public static final int DEFAULT_BUFFER = 1024;
    public static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverChannel;
    private Charset charset = StandardCharsets.UTF_8;
    private List<ChatClientHandler> connectedClients;

    private boolean readyToExit(String msg) {
        return Objects.equals(msg, EXIT);
    }

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
            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
            serverChannel = AsynchronousServerSocketChannel.open(channelGroup);
            connectedClients = new ArrayList<>();

            // 绑定端口
            serverChannel.bind(new InetSocketAddress(DEFAULT_PORT));
            System.out.println("服务端启动成功，监听端口：" + DEFAULT_PORT);

            for (; ; ) {
                serverChannel.accept(null, new AcceptHandler());
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new AioChatServer().start();
    }

    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

        @Override
        public void completed(AsynchronousSocketChannel clientChannel, Object attachment) {
            if (serverChannel.isOpen()) {
                serverChannel.accept(null, this);
            }
            if (clientChannel != null && clientChannel.isOpen()) {
                ChatClientHandler clientHandler = new ChatClientHandler(clientChannel);
                ByteBuffer byteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER);
                // todo 将新用户添加到在线用户列表
                addClient(clientHandler);
                clientChannel.read(byteBuffer, byteBuffer, clientHandler);
            }
        }

        private void addClient(ChatClientHandler clientHandler) {
            connectedClients.add(clientHandler);
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            System.out.println(exc.getMessage());
        }
    }

    private class ChatClientHandler implements CompletionHandler<Integer, Object> {

        private AsynchronousSocketChannel clientChannel;

        public ChatClientHandler(AsynchronousSocketChannel clientChannel) {
            this.clientChannel = clientChannel;
        }

        @Override
        public void completed(Integer result, Object attachment) {
            ByteBuffer buffer = (ByteBuffer) attachment;
            if (buffer != null) {
                if (result <= 0) {
                    // 客户端异常
                    // TODO 将客户移除在线列表

                } else {
                    buffer.flip();
                    String msg = receive(buffer);
                    System.out.println(getClientName(clientChannel) + ":" + msg);
                    forwardMessage(clientChannel, msg);
                }
            }
        }

        private void forwardMessage(AsynchronousSocketChannel clientChannel, String msg) {

        }

        private boolean getClientName(AsynchronousSocketChannel clientChannel) {
            return false;
        }

        private String receive(ByteBuffer buffer) {

            return null;
        }

        @Override
        public void failed(Throwable exc, Object attachment) {

        }
    }
}

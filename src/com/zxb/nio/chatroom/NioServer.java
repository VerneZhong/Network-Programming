package com.zxb.nio.chatroom;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2019-12-18 16:22
 */
public class NioServer {

    private static final int PORT = 8000;

    public void start() throws IOException {
//        1. 创建Selector
        Selector selector = Selector.open();

//        2. 通过ServerSocketChannel创建Channel通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

//        3. 为channel通道绑定监听端口
        serverSocketChannel.bind(new InetSocketAddress(PORT));

//        4. 将Channel设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);

//        5. 将Channel注册到Selector上，监听连接事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动成功！");

//        6. 循环等待新接入的连接
        for (; ; ) {
            // 阻塞获取可用的Channel数量
            int readyChannels = selector.select();

            // todo 为什么要这样？ 为了防止空轮询
            if (readyChannels == 0) {
                continue;
            }

            // 获取可用channel集合
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            // 迭代集合
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();

                // 移除Set中的当前selectionKey
                iterator.remove();

                // 7. 根据就绪状态，调用对应方法处理业务逻辑
                // 如果是接入事件
                if (selectionKey.isAcceptable()) {
                    acceptHandler(serverSocketChannel, selector);
                }

                // 如果是可读事件
                if (selectionKey.isReadable()) {
                    readHandler(selectionKey, selector);
                }
            }
        }

    }

    /**
     * 接入事件处理器
     */
    private void acceptHandler(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        // 如果是接入事件，创建socketChannel
        SocketChannel socketChannel = serverSocketChannel.accept();

        // 将socketChannel设置为非阻塞工作模式
        socketChannel.configureBlocking(false);

        // 将channel注册到selector上，监听可读事件
        socketChannel.register(selector, SelectionKey.OP_READ);

        // 回复客户端提示信息
        socketChannel.write(StandardCharsets.UTF_8.encode("你与聊天室里其他人都不是朋友关系，请注意隐私安全！"));
    }

    /**
     * 可读事件处理器
     */
    private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
        // 要从 selectionKey 中获取已经就绪的channel
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        // 创建 buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        // loop 读取客户端请求信息
        StringBuilder request = new StringBuilder();
        while (socketChannel.read(byteBuffer) > 0) {
            // 切换buffer为读模式
            byteBuffer.flip();

            // 读取buffer中的内容
            request.append(StandardCharsets.UTF_8.decode(byteBuffer));
        }

        // 将channel再次注册到selector上，监听它的可读事件
        socketChannel.register(selector, SelectionKey.OP_READ);

        // 将客户端发送的请求信息
        if (request.length() > 0) {
            // 广播给其他客户端，目前仅输出
            broadCast(selector, socketChannel, request.toString());
        }
    }

    /**
     * 广播给其他客户端
     */
    private void broadCast(Selector selector, SocketChannel socketChannel, String request) {
        // 获取已接入的客户端channel
        Set<SelectionKey> keys = selector.keys();
        // 循环向所有channel广播信息
        keys.forEach(selectionKey -> {
            Channel channel = selectionKey.channel();

            // 剔除发送消息的客户端
            if (channel instanceof SocketChannel && channel != socketChannel) {
                try {
                    ((SocketChannel) channel).write(StandardCharsets.UTF_8.encode((request)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) throws IOException {
        new NioServer().start();
    }
}

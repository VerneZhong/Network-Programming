package com.zxb.nio.chatroom;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * 客户端线程class，专门接收服务器响应信息
 *
 * @author Mr.zxb
 * @date 2019-12-19 15:13
 */
public class NioClientHandler implements Runnable {

    private Selector selector;

    public NioClientHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            for (; ; ) {
                // 阻塞获取可用的Channel数量
                int readyChannels = selector.select();

                // todo 为什么要这样？
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
                    // 如果是可读事件
                    if (selectionKey.isReadable()) {
                        readHandler(selectionKey, selector);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 可读事件处理器
     */
    private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
        // 要从 selectionKey 中获取已经就绪的channel
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        // 创建 buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        // loop 读取服务端请求信息
        StringBuilder resp = new StringBuilder();
        while (socketChannel.read(byteBuffer) > 0) {
            // 切换buffer为读模式
            byteBuffer.flip();

            // 读取buffer中的内容
            resp.append(StandardCharsets.UTF_8.decode(byteBuffer));
        }

        // 将channel再次注册到selector上，监听它的可读事件
        socketChannel.register(selector, SelectionKey.OP_READ);

        // 将服务端发送的响应消息输出
        if (resp.length() > 0) {
            // 广播给其他客户端，目前仅输出
            System.out.println(resp.toString());
        }
    }

}

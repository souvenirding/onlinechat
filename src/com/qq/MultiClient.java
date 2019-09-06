package com.qq;

import java.io.IOException;
import java.net.Socket;

public class MultiClient {
    public static void main(String[] args) throws IOException {
        //1.客户端连接服务器端,返回套接字Socket对象
        Socket socket = new Socket("127.0.0.1", 10666);
        //2.创建读取服务器端信息的线程和发送服务器端信息的线程
        Thread read = new Thread(new ClientReadServer(socket));
        Thread send = new Thread(new ClientSendServer(socket));
        //3.启动线程
        read.start();
        send.start();
    }
}

package com.qq;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiServer {
    public static void main(String[] args) {
        try {
            //1.创建服务器端的ServerSocket对象,等待客户端连接
            ServerSocket serverSocket = new ServerSocket(10666);
            //2.创建线程池,从而可以处理多个客户端
            ExecutorService executorService = Executors.newFixedThreadPool(20);
            for (int i = 0; i < 20; i++) {
                System.out.println("欢迎来到我的聊天室......");
                //3.侦听客户端
                Socket socket = serverSocket.accept();
                System.out.println("有新的朋友加入.....");
                //4.启动线程
                executorService.execute(new Server(socket));
            }
            //5.关闭线程池
            executorService.shutdown();
            //6.关闭服务器
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

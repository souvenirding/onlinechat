package com.qq;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

//1.客户端读取服务器端信息的线程
class ClientReadServer implements Runnable {
    private Socket socket;

    public ClientReadServer(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        //1.获取服务器端输入流
        try {
            Scanner scanner = new Scanner(socket.getInputStream());
            while (scanner.hasNext()) {
                System.out.println(scanner.next());
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

//2.客户端向服务器端发送信息的线程
class ClientSendServer implements Runnable {
    private Socket socket;

    public ClientSendServer(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            //1.获取服务器端的输出流
            PrintStream printStream = new PrintStream(socket.getOutputStream());
            //2.从键盘中输入信息
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String msg = null;
                if (scanner.hasNext()) {
                    msg = scanner.next();
                    printStream.println(msg);
                }
                if (msg.equals("exit")) {
                    scanner.close();
                    printStream.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


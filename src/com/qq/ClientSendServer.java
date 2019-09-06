package com.qq;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Server implements Runnable {
    private static Map<String, Socket> map = new ConcurrentHashMap<>();
    private Socket socket;

    public Server(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        //1.获取客户端的输入流
        try {
            Scanner scanner = new Scanner(socket.getInputStream());
            String msg = null;
            while (true) {
                if (scanner.hasNextLine()) {
                    //0.处理客户端输入的字符串
                    msg = scanner.nextLine();
                    Pattern pattern = Pattern.compile("\r");
                    Matcher matcher = pattern.matcher(msg);
                    msg = matcher.replaceAll("");
                    //1.注册用户流程,注册用户的格式为:username:用户名
                    if (msg.startsWith("username:")) {
                        //将用户名保存在username中
                        String userName = msg.split("\\:")[1];
                        //注册该用户
                        userRegist(userName, socket);
                        continue;
                    }
                    //2.群聊信息流程,群聊的格式为:G:群聊信息
                    else if (msg.startsWith("G:")) {
                        //必须先注册才可以!
                        firstStep(socket);
                        //保存群聊信息
                        String str = msg.split("\\:")[1];
                        //发送群聊信息
                        groupChat(socket, str);
                        continue;
                    }
                    //3.私聊信息流程,私聊的格式为:P:username-私聊信息
                    else if (msg.startsWith("P:") && msg.contains("-")) {
                        //必须先注册才可以!
                        firstStep(socket);
                        //保存需要私聊的用户名
                        String username = msg.split("\\:")[1].split("-")[0];
                        //保存私聊的信息
                        String str = msg.split("\\:")[1].split("-")[1];
                        //发送私聊信息
                        privateChat(socket, username, str);
                        continue;
                    }
                    //4.用户退出流程,用户退出格式为:包含exit
                    else if (msg.contains("exit")) {
                        //必须先注册才可以!
                        firstStep(socket);
                        userExit(socket);
                        continue;
                    }
                    //其他输入格式均错误
                    else {
                        PrintStream printStream = new PrintStream(socket.getOutputStream());
                        printStream.println("输入格式错误!请按照以下格式输入!");
                        printStream.println("注册用户格式:[username:用户名]");
                        printStream.println("群聊格式:[G:群聊信息]");
                        printStream.println("私聊格式:[P:username-私聊信息]");
                        printStream.println("用户退出格式[包含exit即可]");
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 第一步必须先注册!
     *
     * @param socket 当前客户端
     */
    private void firstStep(Socket socket) throws IOException {
        Set<Map.Entry<String, Socket>> set = map.entrySet();
        for (Map.Entry<String, Socket> entry : set) {
            if (entry.getValue().equals(socket)) {
                if (entry.getKey() == null) {
                    PrintStream printStream = new PrintStream(socket.getOutputStream());
                    printStream.println("请先进行注册操作！");
                    printStream.println("注册格式为:[username:用户名]");
                }
            }
        }
    }

    /**
     * 注册用户信息
     *
     * @param username 用户名
     * @param socket   用户客户端Socket对象
     */
    private void userRegist(String username, Socket socket) {
        map.put(username, socket);
        System.out.println("[用户名为" + username + "][客户端为" + socket + "]上线了!");
        System.out.println("当前在线人数为:" + map.size() + "人");
    }

    /**
     * 群聊流程(将Map集合转换为Set集合,从而取得每个客户端Socket,将群聊信息发送给每个客户端)
     *
     * @param socket 发出群聊的客户端
     * @param msg    群聊信息
     */
    private void groupChat(Socket socket, String msg) throws IOException {
        //1.将Map集合转换为Set集合
        Set<Map.Entry<String, Socket>> set = map.entrySet();
        //2.遍历Set集合找到发起群聊信息的用户
        String userName = null;
        for (Map.Entry<String, Socket> entry : set) {
            if (entry.getValue().equals(socket)) {
                userName = entry.getKey();
                break;
            }
        }
        //3.遍历Set集合将群聊信息发给每一个客户端
        for (Map.Entry<String, Socket> entry : set) {
            //取得客户端的Socket对象
            Socket client = entry.getValue();
            //取得client客户端的输出流
            PrintStream printStream = new PrintStream(client.getOutputStream());
            printStream.println(userName + "群聊说:" + msg);
        }
    }

    /**
     * 私聊流程(利用userName取得客户端的Socket对象,从而取得对应输出流,将私聊信息发送到指定客户端)
     *
     * @param socket   当前客户端
     * @param userName 私聊的用户名
     * @param msg      私聊的信息
     */
    private void privateChat(Socket socket, String userName, String msg) throws IOException {
        //1.取得当前客户端的用户名
        String curUser = null;
        Set<Map.Entry<String, Socket>> set = map.entrySet();
        for (Map.Entry<String, Socket> entry : set) {
            if (entry.getValue().equals(socket)) {
                curUser = entry.getKey();
                break;
            }
        }
        //2.取得私聊用户名对应的客户端
        Socket client = map.get(userName);
        //3.获取私聊客户端的输出流,将私聊信息发送到指定客户端
        PrintStream printStream = new PrintStream(client.getOutputStream());
        printStream.println(curUser + "私聊说:" + msg);
    }

    /**
     * 用户退出
     *
     * @param socket
     */
    private void userExit(Socket socket) {
        //1.利用socket取得对应的Key值
        String userName = null;
        for (String key : map.keySet()) {
            if (map.get(key).equals(socket)) {
                userName = key;
                break;
            }
        }
        //2.将userName,Socket元素从map集合中删除
        map.remove(userName, socket);
        //3.提醒服务器该客户端已下线
        System.out.println("用户:" + userName + "已下线!");
    }
}

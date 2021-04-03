import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class testConnect {

    // 服务器端程序
    @Test
    public void testMultiServer() throws IOException {
        // 创建服务器指定8888端口
        ServerSocket server = new ServerSocket(8888);
        Socket client;
        PrintStream out;
        BufferedReader buf;
        //boolean sts = true;// 默认无限循环
        // while循环接收客户端连接
        while (true) {
            System.out.println("======服务器已经运行，等待客户端的链接：======");
            client = server.accept();
            System.out.println("建立客户端连接");
            // 得到客户端的输入信息
            buf = new BufferedReader(new InputStreamReader(client.getInputStream()));
            // 实例化客户端的输出流，用于输出消息
            out = new PrintStream(client.getOutputStream());
            boolean flag = true;
            while (flag) {
                String str = buf.readLine();// 不断接收输入的消息
                if ("exit".equals(str)) { // 客户端结束输入
                    flag = false;
                } else {
                    out.println("echo：" + str);
                }
            }
            out.close();
            client.close();
        }
        //server.close();
    }

    // 客户端程序
    @Test
    public void testClient2() throws IOException {
        // 创建客户端连接,指定服务器的域名和端口
        Socket client = new Socket("localhost", 8888);
        // 接收服务器端的输入
        BufferedReader buf = new BufferedReader(new InputStreamReader(client.getInputStream()));
        // 从键盘接收输入
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        // 向服务器端输出消息
        PrintStream out = new PrintStream(client.getOutputStream());
        boolean flag = true;
        while (flag) {
            System.out.print("请输入消息：");
            String msg = reader.readLine();// 从键盘接收输入的数据
            out.println(msg); // 输出到服务器端
            if ("exit".equals(msg)) {
                flag = false;
            } else {
                System.out.println(buf.readLine()); // 输出服务器返回的消息
            }
        }
        client.close();
        reader.close();
        buf.close();
    }
}

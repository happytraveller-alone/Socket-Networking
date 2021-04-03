import org.junit.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class test {
    // 服务器端程序
    @Test
    public void testServer() throws IOException {
        // 1、创建服务器指定8888端口
        ServerSocket server = new ServerSocket(8888);
        // 2、接收客户端连接,阻塞式
        Socket socket = server.accept();
        System.out.println("建立客户端连接");
        // 3、发送数据
        String msg = "Hello，欢迎使用!";
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bw.write(msg);
        // 必须调用此方法，否则客户端readLine()会执行会发生Connection reset异常
        bw.newLine();
        bw.flush();
        server.close();
    }
    // 客户端程序
    @Test
    public void testClient() throws IOException {
        // 1、创建客户端连接,指定服务器的域名和端口
        Socket client = new Socket("localhost", 8888);
        // 2、接收数据
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        String msg = br.readLine(); // 阻塞式方法
        System.out.println(msg);
        client.close();
    }
}

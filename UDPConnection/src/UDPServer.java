//UDPServer.java
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * @author 123456
 */
public class UDPServer {
    private DatagramSocket socket = null;
    private final int port = 8888;
    private DatagramPacket receivePacket;

    public UDPServer() {
        System.out.println("服务器启动监听在" + port + "端口...");
    }

    public void Service() {
        try {
            socket = new DatagramSocket(port);
            System.out.println("服务器创建成功，端口号：" + socket.getLocalPort());

            while (true) {

                //服务器接收数据
                String msg;
                receivePacket = udpReceive();
                InetAddress ipR = receivePacket.getAddress();
                int portR = receivePacket.getPort();
                msg = new String(receivePacket.getData(), 0, receivePacket.getLength(), StandardCharsets.UTF_8);

                //System.out.println("收到："+receivePacket.getSocketAddress()+"内容："+msg);

                if ("bye".equalsIgnoreCase(msg)) {
                    udpSend("来自服务器消息：服务器断开连接，结束服务！",ipR,portR);
                    System.out.println(receivePacket.getSocketAddress()+"的客户端离开。");
                    continue;
                }
                System.out.println("建立连接："+receivePacket.getSocketAddress());

                String now = new Date().toString();
                String hello = "From 服务器：&" + now + "&" + msg;
                udpSend(hello,ipR,portR);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DatagramPacket udpReceive() throws IOException {
        DatagramPacket receive;
        byte[] dataR = new byte[1024];
        receive = new DatagramPacket(dataR, dataR.length);
        socket.receive(receive);
        return receive;
    }

    public void udpSend(String msg,InetAddress ipRemote,int portRemote) throws IOException {
        DatagramPacket sendPacket;
        byte[] dataSend = msg.getBytes();
        sendPacket = new DatagramPacket(dataSend,dataSend.length,ipRemote,portRemote);
        socket.send(sendPacket);
    }

    public static void main(String[] args) throws IOException {
        new UDPServer().Service();
    }
}
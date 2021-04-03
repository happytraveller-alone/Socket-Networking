//TCPClient.java
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author 123456
 */
public class TCPClient {
    private final Socket socket;

    private final PrintWriter pw;
    private final BufferedReader br;

    public TCPClient(String ip, String port) throws IOException{
        //主动向服务器发起连接，实现TCP三次握手
        //不成功则抛出错误，由调用者处理错误
        socket =new Socket(ip,Integer.parseInt(port));

        //得到网络流输出字节流地址，并封装成网络输出字符流
        OutputStream socketOut=socket.getOutputStream();
        //参数true表示自动flush数据
        pw=new PrintWriter(new OutputStreamWriter(socketOut, StandardCharsets.UTF_8),true);

        //得到网络输入字节流地址，并封装成网络输入字符流
        InputStream socketIn=socket.getInputStream();
        br=new BufferedReader(new InputStreamReader(socketIn, StandardCharsets.UTF_8));

    }

    public void send(String msg){
        //输出字符流，由socket调用系统底层函数，经网卡发送字节流
        pw.println(msg);
    }

    public String receive(){
        String msg=null;
        try {
            //从网络输入字符流中读取信息，每次只能接受一行信息
            //不够一行时（无行结束符），该语句阻塞
            //直到条件满足，程序往下运行
            msg=br.readLine();
        }catch (IOException e){
            e.printStackTrace();
        }
        return msg;
    }

    public void close(){
        try {
            if (socket!=null){
                socket.close();}
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
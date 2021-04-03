import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** @author 123456 */
public class TcpThreadServer {
  /** 定义服务器套接字 */
  private final ServerSocket serverSocket;
  /** 创建动态线程池，适合小并发量，容易出现OutOfMemoryError */
  private ExecutorService executorService = Executors.newCachedThreadPool();

  public TcpThreadServer() throws IOException {
    int port = 12345;
    serverSocket = new ServerSocket(port,50);
    // 服务器监听窗口
    System.out.println("服务器启动监听在" + port + "端口...");
  }

  public static void main(String[] args) throws IOException {
    new TcpThreadServer().service();
  }

  private PrintWriter getWriter(Socket socket) throws IOException {
    // 获得输出流缓冲区的地址
    OutputStream socketOut = socket.getOutputStream();
    // 网络流写出需要使用flush，这里在printWriter构造方法直接设置为自动flush
    return new PrintWriter(new OutputStreamWriter(socketOut, StandardCharsets.UTF_8), true);
  }

  private BufferedReader getReader(Socket socket) throws IOException {
    // 获得输入流缓冲区的地址
    InputStream socketIn = socket.getInputStream();
    return new BufferedReader(new InputStreamReader(socketIn, StandardCharsets.UTF_8));
  }

  /** 多客户版本，可以同时与多用户建立通信连接 */
  public void service() throws IOException {
    while (true) {
      Socket socket;
      socket = serverSocket.accept();
      // 将服务器和客户端的通信交给线程池处理
      Handler handler = new Handler(socket);
      executorService.execute(handler);
    }
  }

  class Handler implements Runnable {
    private final Socket socket;
    public Handler(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      // 本地服务器控制台显示客户端连接的用户信息
      System.out.println("New connection accept:" + socket.getInetAddress());
      try {
        BufferedReader br = getReader(socket);
        PrintWriter pw = getWriter(socket);
        pw.println("From 服务器：欢迎使用服务！");
        String msg;
        while ((msg = br.readLine()) != null) {
          if ("bye".equalsIgnoreCase(msg.trim())) {
            pw.println("From 服务器：服务器已断开连接，结束服务！");
            System.out.println("客户端离开。");
            break;
          }
          pw.println("From 服务器：" + msg);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;


/** @author 123456 */
public class TcpThreadServer {
  /** 定义服务器套接字 */
  private final ServerSocket serverSocket;
  /** 创建动态线程池 */
  ExecutorService pool = new ThreadPoolExecutor(5, 200, 0L, TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<Runnable>(1024), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
  /**hashmap用来保存socket和用户名*/
  private ConcurrentHashMap<Socket,String> users=new ConcurrentHashMap();

  /** 主方法调用函数 */
  public static void main(String[] args) throws IOException {
    new TcpThreadServer().service();
  }

  /** 多客户版本，可以同时与多用户建立通信连接 */
  public void service() throws IOException {
    while (true) {
      Socket socket;
      socket = serverSocket.accept();
      // 将服务器和客户端的通信交给线程池处理
      Handler handler = new Handler(socket);
      //executorService.execute(handler);
      pool.execute(handler);
    }
  }

  /**多线程处理程序 */
  class Handler implements Runnable {
    private Socket socket;
    private String localName = null;
    private String hostName = null;
    private boolean flag;
    private boolean isExist;

    public Handler(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      //本地服务器控制台显示客户端连接的用户信息
      System.out.println("New connection accept:" + socket.getInetAddress());//.getHostAddress()
      try {
        BufferedReader br = getReader(socket);
        PrintWriter pw = getWriter(socket);

        pw.println("服务器：欢迎使用服务！");
        pw.println("请输入用户名：");

        while ((hostName=br.readLine())!=null){
          users.forEach((k,v)->{
            if (v.equals(hostName)){
              flag=true;}//线程修改了全局变量
          });

          if (!flag){
            localName=hostName;
            users.put(socket,hostName);
            flag=false;//可能找出不一致问题
            break;
          }
          else{
            flag=false;
            pw.println("该用户名已存在，请修改！");
          }
        }

        //System.out.println(hostName+": "+socket);
        sendToMembers("我已上线",localName,socket);
        //pw.println("输入命令功能：(1)L:查看当前上线用户;(2)G:进入群聊;(3)P:私信;(4)E:退出当前聊天状态;(5)bye:离线;(6)H:帮助");

        String msg;
        //用户连接服务器上线，进入聊天选择状态
        while ((msg = br.readLine()) != null) {
          if ("bye".equalsIgnoreCase(msg.trim())) {
            pw.println("From 服务器：服务器已断开连接，结束服务！");

            users.remove(socket,localName);

            sendToMembers("我下线了",localName,socket);
            System.out.println("客户端离开。");//加当前用户名
            break;
          }
          else if ("L".equalsIgnoreCase(msg.trim())){
            users.forEach((k,v)-> pw.println("用户:"+v));
          }
          //一对一私聊
          else if ("P".equalsIgnoreCase(msg.trim())){
            pw.println("请输入私信人的用户名：");
            String name=br.readLine();

            //查找map中匹配的socket，与之建立通信
            users.forEach((k, v)->{
              if (v.equals(name)) {
                isExist=true;//全局变量与线程修改问题
              }

            });
            //已修复用户不存在的处理逻辑
            Socket temp=null;
            for(Map.Entry<Socket,String> mapEntry : users.entrySet()){
              if(mapEntry.getValue().equals(name)){
                temp = mapEntry.getKey();}
            }
            if (isExist){
              isExist=false;
              //私信后有一方用户离开，另一方未知，仍然发信息而未收到回复，未处理这种情况
              while ((msg=br.readLine())!=null){
                if (!"E".equals(msg)&&!isLeaved(temp)){
                  sendToOne(msg,localName,temp);}
                else if (isLeaved(temp)){
                  pw.println("对方已经离开，已断开连接！");
                  break;
                }
                else{
                  pw.println("您已退出私信模式！");
                  break;
                }
              }
            }
            else{
              pw.println("用户不存在！");}
          }
          //选择群聊
          else if ("G".equals(msg.trim())){
            pw.println("您已进入群聊。");
            while ((msg=br.readLine())!=null){
              if (!"E".equals(msg)&&users.size()!=1){
                sendToMembers(msg,localName,socket);}
              else if (users.size()==1){
                pw.println("当前群聊无其他用户在线，已自动退出！");
                break;
              }
              else {
                pw.println("您已退出群组聊天室！");
                break;
              }
            }

          }
          else{
            pw.println("请选择聊天状态！");}
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public TcpThreadServer() throws IOException {
    int port = 12345;
    serverSocket = new ServerSocket(port,50);
    // 服务器监听窗口
    System.out.println("服务器启动监听在" + port + "端口...");
  }

  private void sendToOne(String msg,String hostAddress,Socket another) throws IOException{

    PrintWriter pw;
    OutputStream out;

    for (Map.Entry<Socket, String> socketStringEntry : users.entrySet()) {

      Socket tempSocket = (Socket) ((Map.Entry) socketStringEntry).getKey();

      if (tempSocket.equals(another)) {
        out = tempSocket.getOutputStream();
        pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        pw.println(hostAddress + "私信了你：" + msg);
      }
    }
  }

  private void sendToMembers(String msg,String hostAddress,Socket mySocket) throws IOException{

    PrintWriter pw;
    OutputStream out;
    for (Map.Entry<Socket, String> socketStringEntry : users.entrySet()) {
      Socket tempSocket = (Socket) ((Map.Entry) socketStringEntry).getKey();
      if (!tempSocket.equals(mySocket)) {
        out = tempSocket.getOutputStream();
        pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);
        pw.println(hostAddress + "：" + msg);
      }
    }

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

  /**判断用户是否已经下线*/
  private Boolean isLeaved(Socket temp){
    boolean leave=true;
    for(Map.Entry<Socket,String> mapEntry : users.entrySet()) {
      if (mapEntry.getKey().equals(temp)) {
        leave = false;
        break;
      }
    }
    return leave;
  }


}

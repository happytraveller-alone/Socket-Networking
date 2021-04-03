import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TcpClientFx.java Copyright (c) 2021-3-30
 *
 * @author : 12345 All right reserved.
 */
public class TcpClientFXFinal extends Application {
  private final Button ButtonExit = new Button("退出");
  private final Button ButtonSend = new Button("发送");
  private final Button ButtonConnect = new Button("连接");
  private final Button ButtonRegister = new Button("注册");
  private final String ExitWord = "bye";
  private final String Port = "12345";
  private TcpClient tcpClient;
  private Thread readThread;
  private String receiveMsg;

  /** 输入信息区域 */
  private final TextField TextFieldSend = new TextField();
  /** 显示区域 */
  private final TextArea TextAreaDisplay = new TextArea();
  /** 填写ip地址 */
  private final TextField ipAddress = new TextField();
  /**填写端口*/
  private final TextField TextFieldPort = new TextField();
  /**填写端口*/
  private final TextField TextFieldName = new TextField();


  /**
   * 主函数调用
   */
  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
      //设置窗口名称
      primaryStage.setTitle("网络聊天室");
      //创建窗口
      BorderPane mainPane = new BorderPane();
      //设置应用程序窗口大小
      Scene scene = new Scene(mainPane, 800, 500);
      //把场景放入窗口内
      primaryStage.setScene(scene);
      //显示
      primaryStage.show();

      //连接服务器区域
      HBox hBox1 = new HBox();
      //实例居顶部
      mainPane.setTop(hBox1);
      //设置间距
      hBox1.setSpacing(10);
      ////Insets(top,right,bottom,left)使用四个不同的偏移量构造预连接区域实例。
      hBox1.setPadding(new javafx.geometry.Insets(10, 20, 10, 20));
      //设置两个属性值：IP地址和端口号位置居中
      hBox1.setAlignment(Pos.CENTER);
      //添加控件，包括IP地址文字，输入框，端口文字，输入框，连接按钮
      hBox1.getChildren()
            .addAll(new Label("ip地址："),
                    ipAddress,
                    new Label("端口："),
                    TextFieldPort,
                    ButtonConnect,
                    new Label("昵称："),
                    TextFieldName,
                    ButtonRegister
                    );
      //自动获取客户端主机的IP地址，并进行文本框内容填充
      InetAddress address;
      String ip = null;
      try {
          address = InetAddress.getLocalHost();
          ip = address.getHostAddress();
      } catch (UnknownHostException e) {
          e.printStackTrace();
      }
      AtomicReference<String> finalIp = new AtomicReference<>(ip);
      ipAddress.setText(finalIp.get());
      //自动填充端口号为12345，同主机进行同步
      TextFieldPort.setText(Port);
      TextFieldName.setText("NULL");


      //设置信息显示区和信息输入区
      VBox vBox = new VBox();
      //实例居中显示
      mainPane.setCenter(vBox);
      //文本行间距
      vBox.setSpacing(10);
      //Insets(top,right,bottom,left)使用四个不同的偏移量构造信息显示区实例。
      vBox.setPadding(new javafx.geometry.Insets(10, 20, 10, 20));
      //示例内部添加控件，包括信息显示区文字，信息显示文本框，信息输入区文字，信息输入文本框
      vBox.getChildren()
            .addAll(
                    new javafx.scene.control.Label("信息显示区"),
                    TextAreaDisplay,
                    new Label("信息输入区"),
                    TextFieldSend);
      //信息显示区控件设置为在调整父VBox的高度时垂直增长
      VBox.setVgrow(TextAreaDisplay, Priority.ALWAYS);
      //设置信息显示区文本框为不可编辑
      TextAreaDisplay.setEditable(false);

      //设置底部实例
      HBox hBox = new HBox();
      //设置实例在底部
      mainPane.setBottom(hBox);
      //设置控件间距
      hBox.setSpacing(10);
      //Insets(top,right,bottom,left)使用四个不同的偏移量构造底部实例。
      hBox.setPadding(new javafx.geometry.Insets(10, 20, 10, 20));
      //实例居右显示
      hBox.setAlignment(Pos.CENTER_LEFT);
      //添加发送按钮和退出按钮两个控件
      Text text = new Text("输入命令功能：(1)L:查看当前上线用户;(2)G:进入群聊;" +
              "(3)P:私信;(4)E:退出当前聊天状态;(5)bye:离线;");
      text.setFill(Color.INDIANRED);
      text.setFont(new Font(15));
      hBox.getChildren().
              addAll(text,ButtonSend, ButtonExit);

      //连接按钮事件
      ButtonConnect.setOnAction(event -> {
          String IP = ipAddress.getText().trim();
          try {
              // tcpClient是本程序定义的一个TCPClient类型的成员变量
              tcpClient = new TcpClient(IP, Port);
              // 用于接收服务器信息的单独线程
              readThread = new Thread(() -> {
                  while ((receiveMsg = tcpClient.receive()) != null) {
                      Platform.runLater(() -> TextAreaDisplay.appendText(receiveMsg + "\n"));
                  }
                  Platform.runLater(() -> TextAreaDisplay.appendText("对话已关闭！\n"));
              });

              readThread.start();
              // 连接服务器之后未结束服务前禁用再次连接
              ButtonConnect.setDisable(true);
              // 重新连接服务器时启用输入发送功能
              TextFieldSend.setDisable(false);
              ButtonSend.setDisable(false);
            } catch (Exception e) {
              TextAreaDisplay.appendText("服务器连接失败！" + e.getMessage() + "\n");
          }
      });


      //连接按钮事件
      ButtonRegister.setOnAction(event -> {
          String msg = TextFieldName.getText().trim();
          while (msg.length() <= 4){
              TextAreaDisplay.appendText("昵称名称过短\n请您重新输入\n");
              TextFieldSend.clear();
              msg = TextFieldName.getText().trim();
          }
          tcpClient.send(msg); // 向服务器发送一串字符
          TextAreaDisplay.appendText("您的昵称是：" + msg + "\n");
      });

      //发送按钮事件
      ButtonSend.setOnAction(event -> {
            String msg = TextFieldSend.getText();
            tcpClient.send(msg); // 向服务器发送一串字符
            TextAreaDisplay.appendText("客户端发送：" + msg + "\n");
            if (ExitWord.equalsIgnoreCase(msg)) {
                // 发送bye后禁用发送按钮
                ButtonSend.setDisable(true);
                // 禁用Enter发送信息输入区域
                TextFieldSend.setDisable(true);
                // 结束服务后再次启用连接按钮
                ButtonConnect.setDisable(false);
            }
            TextFieldSend.clear();
      });

      // 对输入区域绑定键盘事件
      TextFieldSend.setOnKeyPressed(event -> {
          if (event.getCode() == KeyCode.ENTER) {
              String msg = TextFieldSend.getText();
              tcpClient.send(msg); // 向服务器发送一串字符
              TextAreaDisplay.appendText("客户端发送：" + msg + "\n");

              if (ExitWord.equalsIgnoreCase(msg)) {
              // 禁用Enter发送信息输入区域
              TextFieldSend.setDisable(true);
              // 发送bye后禁用发送按钮
              ButtonSend.setDisable(true);
              // 结束服务后再次启用连接按钮
              ButtonConnect.setDisable(false);
              }
              TextFieldSend.clear();
          }
      });

      ButtonExit.setOnAction(event -> {
          try {
            exit();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
      });

      // 窗体关闭响应的事件,点击右上角的×关闭,客户端也关闭
      primaryStage.setOnCloseRequest(event -> {
          try {
            exit();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
      });
  }

  private void exit() throws InterruptedException {
      if (tcpClient != null) {
      tcpClient.send(ExitWord);
      //多线程等待，关闭窗口时还有线程等待IO，设置1s间隔保证所有线程已关闭
      Thread.sleep(1000);
      tcpClient.close();
    }
    System.exit(0);
  }
}

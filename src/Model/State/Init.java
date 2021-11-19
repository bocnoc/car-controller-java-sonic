package Model.State;

import Model.CarModel;
import Util.PWMDevice.Steer;
import Util.PWMDevice.Throttle;

import java.io.*;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

public class Init extends State {
    private final static State state = new Init();

    public static State getInstance() {
        return state;
    }

    private void start(final Steer steer, final Throttle throttle) {
        // TODO: テキストファイルとかで走行を定義できるようにしたほうがいいかも
        try {
            throttle.setScale(-1.0);
            Thread.sleep(2000);
            throttle.setScale(0.0);
            steer.setScale(1.0);
            Thread.sleep(500);
            throttle.setScale(-1.0);
            Thread.sleep(1000);
            throttle.setScale(0);
            steer.setScale(0);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * localhost:8888にリクエストを飛ばすと次の状態に遷移する
     * 認証とかは実装していないのでよくないが，そこは本質ではないのでヨシ!!
     * @param model
     */
    @Override
    public void doAction(CarModel model) {
        System.out.println(this);
        // wait request
        try ( // try-with-resource: tryの中でAutoClosableを実装したメソッドを呼び出すと，リソースを使わなくなったときにソケットとかを自動で閉じてくれる
              final var server = new ServerSocket(8888);
              final var socket = server.accept();
              final var input = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
              final var output = new BufferedWriter(new OutputStreamWriter((socket.getOutputStream())));
        ) {
            var line = input.readLine();
            while (line != null && !line.isEmpty()) {
                line = input.readLine();
            }
            output.write("HTTP/1.1 200 OK\r\n");
        } catch (IOException e) {
            model.setState(Halt.getInstance());
            e.printStackTrace();
            return;
        }
        // start
        this.start(model.getSteer(), model.getThrottle());
        model.setState(Halt.getInstance());
    }

    @Override
    public String toString() {
        return "INIT";
    }
}

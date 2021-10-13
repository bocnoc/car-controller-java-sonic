package Util.PWMDevice;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * PWM関連のデバイスを定義するための抽象クラス
 */
public abstract class PWMDevice {
    final String port;
    final String host;
    final String deviceName;

    public PWMDevice(String deviceName, Properties properties) {
       // init properties here...
        this.deviceName = deviceName;
        this.port = properties.getProperty("serverPort", "1065");
        this.host = properties.getProperty("serverHostName", "1.1.1.1");
        this.setScale(0);
    }

    public abstract void setScale(double scale);

    /**
     * http://host:port/device/scale にリクエストを送ることでハードウェア制御を行います
     * リクエストに失敗した場合は何も起こりません
     * @param scale PWMを-1.0~1.0のスケール値で入力します．範囲外の数値を入力した場合，サーバ側で-1.0もしくは1.0に値が変更されます
     */
    void sendRequest(double scale) {
        final var client = HttpClient.newHttpClient();
        final var uri = String.format("http://%s:%s/%s/%f", host, port, deviceName, scale);
        final var request = HttpRequest.newBuilder(URI.create(uri)).build();
        final var future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        try {
            future.get(1000, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            // リクエスト失敗したら車両が動かないだけなので何もしなくても問題なし
            System.out.println(deviceName + ": request failed");
        }
    }
}

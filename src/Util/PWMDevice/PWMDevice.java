package Util.PWMDevice;

import java.util.Properties;

public abstract class PWMDevice {
    final String port;
    final String host;


    public PWMDevice(Properties properties) {
       // init properties here...
        this.port = properties.getProperty("serverPort", "1065");
        this.host = properties.getProperty("serverHostName", "1.1.1.1");
    }

    public abstract void setScale(double scale);

    void sendRequest(double pwmVal) {
        System.out.println(pwmVal);
    }
}

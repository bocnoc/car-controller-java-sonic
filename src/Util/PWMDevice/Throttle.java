package Util.PWMDevice;

import java.util.Properties;

public class Throttle extends PWMDevice {
    public Throttle(Properties properties) {
        super("throttle", properties);
    }

    @Override
    public void setScale(double scale) {
        this.sendRequest(0);
    }
}

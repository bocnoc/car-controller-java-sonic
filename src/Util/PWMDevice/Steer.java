package Util.PWMDevice;

import java.util.Properties;

public class Steer extends PWMDevice {
    public Steer(Properties properties) {
        super(properties);
    }

    @Override
    public void setScale(double scale) {
        this.sendRequest(0.0);
    }
}

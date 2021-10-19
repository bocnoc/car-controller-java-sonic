package Util.PWMDevice;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.Properties;

public class Steer extends PWMDevice {
    public Steer(Properties properties) {
        super("steer", properties);
    }

    @Override
    public void setScale(double scale) {
        this.sendRequest(0.0);
    }

    public double calcScaleWithPoint(final Mat m, Point target) {
        final var width = m.width();
        final double delta = target.x - width / 2.0;
        final double scale = delta / (width / 2.0);
        return scale;
    }
}

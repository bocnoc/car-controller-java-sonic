package Util.PWMDevice;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import Model.*;
import java.util.Properties;

public class Steer extends PWMDevice {
    public Steer(Properties properties) {
        super("steer", properties);
    }

    @Override
    public void setScale(double scale) {
        this.sendRequest(scale);
    }

    public double calcScaleWithPoint(final Mat m, Point target) {

        final var width = m.width(); //number of columm getting from RGB camera
        final double delta = target.x - width / 2.0; //distance between centerpoint and car's position
        final double scale = (delta / (width / 2.0)) - 0.2; //calculate steering scale
        //System.out.println("width" + width);

        return scale;
    }

/*
    public double SteerForRetry(final fromleftorright) {
      if(left()){
          scale = -0,5;
      } else {
          scale = 0.5;
      }
      return scale;

    }
*/
}


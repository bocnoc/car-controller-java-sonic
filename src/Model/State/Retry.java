package Model.State;

import Model.CarModel;
import Util.ArUcoMarker;
import Util.Gpio;
import Util.PathPlanning;
import Model.GPIO.Ultrasonic_distance;
import org.intel.rs.frame.FrameList;
import org.intel.rs.processing.Align;
import org.intel.rs.processing.HoleFillingFilter;
import org.intel.rs.types.Pixel;
import org.intel.rs.types.Stream;
import org.intel.rs.types.Vertex;
import org.intel.rs.util.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import Model.*;


import java.io.IOException;

public class Retry extends State {

    final static Retry state = new Retry();

    public static Retry  getInstance() {
        return state;
    }

    @Override
    public void doAction(CarModel model) {
        try { // 充電器の起動までにはラグがあるので待つ
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try (final var gpio = new Gpio("194", Gpio.Mode.IN)) {
            final var gpioState = gpio.read();
            switch (gpioState) {
                case HIGH -> {
                    model.setState(Init.getInstance());
                    return;
                }
                case LOW -> model.setState(Tracking.getInstance());
                case ILL -> {
                    model.setState(Halt.getInstance());
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            model.setState(Halt.getInstance());
            return;
        }

        final var streamManager = model.getStreamManager();
        final var align = new Align(Stream.Color);
        final var steer = model.getSteer();
        final var throttle = model.getThrottle();
        //final var scale = steer.calcScaleWithPoint(colorMat, new Point(targetScreenPoint.getI(), targetScreenPoint.getJ()));
        //steer.SteerForRetry(scale);
        //forback forbac = new forback();
        /*newline add*/
        double Steer_of_sonic = Model.GPIO.Ultrasonic_distance.Sonic_distance();

        System.out.println(Steer_of_sonic);
        steer.setScale(Steer_of_sonic);

        /*end of added line*/
        //steer.setScale(0);
        throttle.setScale(-0.5);

        while (true) {
            final FrameList data;
            {
                final var rawData = streamManager.getAlignedFrameList();
                data = align.process(rawData);
                rawData.release();
            }
            final var colorFrame = data.getColorFrame();
            final var depthFrame = data.getDepthFrame();
            final Mat colorMat = new Mat(colorFrame.getHeight(), colorFrame.getWidth(), CvType.CV_8UC3, colorFrame.getData());
            final ArUcoMarker marker = PathPlanning.detectMarker(colorMat);
            if (marker != null && marker.corners().size() == 4) {
                final var corner = marker.corners();
                final var centerPoint = marker.center();
                final var centerDepth = depthFrame.getDistance((int) centerPoint.x, (int) centerPoint.y);
                final var leftX = (corner.get(0).x + corner.get(3).x) / 2;
                final var leftY = (corner.get(0).y + corner.get(3).y) / 2;
                final var rightX = (corner.get(1).x + corner.get(2).x) / 2;
                final var rightY = (corner.get(1).y + corner.get(2).y) / 2;
                final var leftDepth = depthFrame.getDistance((int) leftX, (int) leftY);
                final var rightDepth = depthFrame.getDistance((int) rightX, (int) rightY);
                if (centerDepth != 0 && leftDepth != 0 && rightDepth != 0) {
                    final var intrinsics = streamManager.getCameraIntrinsics();
                    // ↓ RealSenseの内部パラメタ(intrinsics)を用いて，マーカ上の点を，3次元座標に変換
                    final var left3D = Utils.deprojectPixelToPoint(intrinsics, new Pixel((int) leftX, (int) leftY), leftDepth);
                    final var center3D = Utils.deprojectPixelToPoint(intrinsics, new Pixel((int) centerPoint.x, (int) centerPoint.y), centerDepth);
                    final var right3D = Utils.deprojectPixelToPoint(intrinsics, new Pixel((int) rightX, (int) rightY), rightDepth);
                    // ↓ 車が向かうべき点の算出
                    final var targetPoint = PathPlanning.calcTargetPoint(left3D, center3D, right3D, 0.8); // TODO: dをプロパティで変更できるようにする
                    final var targetScreenPoint = Utils.projectPointToPixel(intrinsics, new Vertex((float) targetPoint.x, center3D.getY(), (float) targetPoint.y));
                    //System.out.println(centerDepth);
                    if (centerDepth > 2.0) {
                        steer.setScale(0);
                        throttle.setScale(0);
                        break;
                    } else if (centerDepth < 2.0 && centerDepth >= 1) {
                        if (targetScreenPoint.getI() >= colorFrame.getWidth() / 3.0 || targetScreenPoint.getI() < (colorFrame.getWidth() / 3.0) * 2.0 ||
                                targetScreenPoint.getJ() >= 0 || targetScreenPoint.getJ() < colorFrame.getHeight()
                        ) {
                            final var scale = steer.calcScaleWithPoint(colorMat, new Point(targetScreenPoint.getI(), targetScreenPoint.getJ()));
                            steer.setScale(-scale/2); //backing steering too much, decrease the scale to half
                                System.out.print(scale);

                        } else if (targetScreenPoint.getI() <= colorFrame.getWidth() / 3.0) { // 点が画面の左の方にあるとき -1
                            steer.setScale(0);
                        } else if (targetScreenPoint.getI() > (colorFrame.getWidth() / 3.0) * 2.0 ) { // 点が画面の右の方にあるとき 1
                            steer.setScale(0);
                        }

                    } else if (centerDepth < 1){
                        steer.setScale(Steer_of_sonic); //from 0 to steer_of_sonic
                    }

                    intrinsics.release();
                }
                data.release();
                colorFrame.release();
                depthFrame.release();
                colorMat.release();
            }
        }
    }

    @Override
    public String toString() {
        return "RETRY";
    }
}

package Model.State;

import Model.CarModel;
import Util.PWMDevice.Steer;
import Util.PWMDevice.Throttle;
import Util.PathPlanning;
import org.intel.rs.frame.DepthFrame;
import org.intel.rs.frame.FrameList;
import org.intel.rs.processing.Align;
import org.intel.rs.processing.Colorizer;
import org.intel.rs.processing.HoleFillingFilter;
import org.intel.rs.types.Stream;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class Run extends State {
    private static final State state = new Run();
    private boolean isDetectingMarker = false;

    public static State getInstance() {
        return state;
    }

    private void rotate(Throttle throttle, Steer steer) {
        System.out.println("Turn");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //throttle.setScale(0);
            e.printStackTrace();
        }
        this.isDetectingMarker = true;
    }

    @Override
    public void doAction(CarModel model) {
        System.out.println(this);
        final var stream = model.getStreamManager();
        final var alignTo = new Align(Stream.Color);
        final Colorizer colorizer = new Colorizer();
        final HoleFillingFilter holeFillingFilter = new HoleFillingFilter();
        final var property = model.getProperties();
        final var steer = model.getSteer();
        final var throttle = model.getThrottle();

        while (true) {
            final FrameList data;
            {
                final var raw_data= stream.getFrameList();
                data = alignTo.process(raw_data);
                raw_data.release();
            }
            final var color = data.getColorFrame();
            final DepthFrame depth;
            {
                final var depth_raw = data.getDepthFrame();
                depth = holeFillingFilter.process(depth_raw);
                depth_raw.release();
            }

            final var depthFrame = colorizer.colorize(depth);
            final Mat colorMat = new Mat(color.getHeight(), color.getWidth(), CvType.CV_8UC3, color.getData());
            final Mat depthMat = new Mat(color.getHeight(), color.getWidth(), CvType.CV_8UC3, depthFrame.getData());

            // calculate scale
            if (/*this.isDetectingMarker*/true) {
                // aruco marker detecting
                final var marker = PathPlanning.detectMarker(colorMat);
                final var threshold = 1.0;
                if (marker != null) {
                    final var p = marker.center();
                    System.out.println(steer.calcScaleWithPoint(colorMat, p));
                }
                /*if (point != null && depth.getDistance((int) point.x, (int) point.y) < threshold) {
                    this.isDetectingMarker = false;
                    model.setState(Tracking.getInstance());
                    depthFrame.release();
                    colorMat.release();
                    depthMat.release();
                    color.release();
                    depth.release();
                    colorMat.release();
                    data.release();
                    break;
                }*/
                if (depth.getDistance(0, 0) > 10000) {
                    break;
                }
            }
            // check center wall -> turn
            final var detectThreshold = Double.parseDouble(property.getProperty("wallDetectThreshold", "0.7"));
            final var centerWallPoints = PathPlanning.getCenterWallPointArray(depth, detectThreshold);
            centerWallPoints.forEach(point -> {
                Imgproc.circle(depthMat, point, 5, new Scalar(255, 255, 255), Imgproc.FILLED);
            });
            //rotate(throttle, steer);
            // control steering
            final var wallPoints = PathPlanning.getWallOfPath(depth, detectThreshold, 8);

            //final double averageX = points.stream().mapToDouble(point -> point.x).sum() / points.size();
            // TODO: calc steer scale
            // draw
            PathPlanning.drawWall(depthMat, wallPoints);
            PathPlanning.drawWall(colorMat, wallPoints);

            final Mat out = new Mat();
            final var l = List.of(colorMat, depthMat);
            Core.hconcat(l, out);
            l.forEach(Mat::release);
            model.pushFrameToQueue(out);
            // release
            depthFrame.release();
            colorMat.release();
            depthMat.release();
            color.release();
            depth.release();
            colorMat.release();
            data.release();
        }
        holeFillingFilter.release();
        colorizer.release();
        alignTo.release();
    }


    @Override
    public String toString() {
        return "RUN";
    }
}

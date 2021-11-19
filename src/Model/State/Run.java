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
import org.opencv.core.*;
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
        for (int i = 0; i < 10; i++) {
            try {

                System.out.println("Start");
                steer.setScale(-1.0);
                Thread.sleep(1000);

                throttle.setScale(-0.5);
                Thread.sleep(500);

                steer.setScale(1.0);
                throttle.setScale(0.0);
                Thread.sleep(1000);

                throttle.setScale(0.5);
                Thread.sleep(500);

                steer.setScale(1.0);
                throttle.setScale(0);
                Thread.sleep(1000);
                System.out.println("End");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.isDetectingMarker = true;
        throttle.setScale(0.8);
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
        //throttle.setScale(0.8);
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

            final var detectThreshold = Double.parseDouble(property.getProperty("wallDetectThreshold", "0.7"));
            final var walls = PathPlanning.getWallOfPath(depth, detectThreshold, 8);
            final double averageX = walls.stream().mapToDouble(wall -> wall.getCenterPoint().x).sum() / walls.size();
            steer.setScale(steer.calcScaleWithPoint(depthMat, new Point(averageX, 0)) * 1.5 - 0.2);

            if (this.isDetectingMarker) {
                // aruco marker detecting
                final var marker = PathPlanning.detectMarker(colorMat);
                final var threshold = 3.0;
                if (marker != null && depth.getDistance((int) marker.center().x, (int) marker.center().y) < threshold) {
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
                }
            } else {
                // check center wall -> turn
                final var centerWallPoints = PathPlanning.getCenterWallPointArray(depth, detectThreshold);
                if (centerWallPoints.size() > 20) {
                    rotate(throttle, steer);
                }
                PathPlanning.drawPoints(depthMat, centerWallPoints);
            }
            // calculate scale
            final var wallPoints = PathPlanning.getWallOfPath(depth, detectThreshold, 8);
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

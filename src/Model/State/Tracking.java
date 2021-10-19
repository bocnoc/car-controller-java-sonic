package Model.State;

import Model.CarModel;
import Util.ArUcoMarker;
import Util.PathPlanning;
import org.intel.rs.processing.Align;
import org.intel.rs.types.Pixel;
import org.intel.rs.types.Stream;
import org.intel.rs.util.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class Tracking extends State {
    private final static State state = new Tracking();

    public static State getInstance() {
        return state;
    }

    @Override
    public void doAction(CarModel model) {
        System.out.println(this);
        final var holeFillingFilter = new Align(Stream.Color);
        final var streamManager = model.getStreamManager();
        while (true) {
            final var data = streamManager.getAlignedFrameList();
            final var colorFrame = data.getColorFrame();
            final var depthFrame = data.getDepthFrame();
            final Mat colorMat = new Mat(colorFrame.getHeight(), colorFrame.getWidth(), CvType.CV_8UC3, colorFrame.getData());
            final ArUcoMarker marker =  PathPlanning.detectMarker(colorMat);
            final Mat out = new Mat();
            colorMat.copyTo(out);
            if (marker != null) {
                final var centerPoint = marker.center();
                final var centerDepth = depthFrame.getDistance((int) centerPoint.x, (int)centerPoint.y);
                if (centerDepth != 0) {
                    final var intrinsics = streamManager.getCameraIntrinsics();
                    if (intrinsics != null && intrinsics.getInstance() != null) {
                       //final var point = Utils.deprojectPixelToPoint(intrinsics, /*new Pixel((int) centerPoint.x, (int) centerPoint.y)*/null, centerDepth);
                        //System.out.printf("x: %f y: %f z: %f\n", point.getX(), point.getY(), point.getZ());
                        intrinsics.release();
                    }
                }
                Imgproc.circle(out, marker.center(), 5, new Scalar(255, 255, 0), Imgproc.FILLED);
            }
            model.pushFrameToQueue(out);
            colorMat.release();
            colorFrame.release();
            depthFrame.release();
            data.release();
        }
        // holeFillingFilter.release();
        // model.setState(Halt.getInstance());
    }

    @Override
    public String toString() {
        return "TRACKING";
    }
}

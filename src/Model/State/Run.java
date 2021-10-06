package Model.State;

import Model.CarModel;
import org.intel.rs.processing.Colorizer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.List;

public class Run extends State {
    private static final State state = new Run();

    public static State getInstance() {
        return state;
    }

    @Override
    public void doAction(CarModel model) {
        System.out.println(this);
        final var stream = model.getStreamManager();
        while (true) {
            final var data = stream.getFrameList();
            final var color = data.getColorFrame();
            final var depth = data.getDepthFrame();
            final Colorizer colorizer = new Colorizer();
            final var depthFrame = colorizer.colorize(depth);
            final Mat colorMat = new Mat(color.getHeight(), color.getWidth(), CvType.CV_8UC3, color.getData());
            final Mat depthMat = new Mat(color.getHeight(), color.getWidth(), CvType.CV_8UC3, depthFrame.getData());
            final Mat out = new Mat();
            final var l = List.of(colorMat, depthMat);
            Core.hconcat(l, out);
            model.pushFrameToQueue(out);
            depthFrame.release();
            colorMat.release();
            depthMat.release();
            color.release();
            depth.release();
            colorMat.release();
            data.release();
            colorizer.release();
        }
        // model.setState(Tracking.getInstance());
    }

    @Override
    public String toString() {
        return "RUN";
    }
}

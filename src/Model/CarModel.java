package Model;

import Model.State.Halt;
import Model.State.Init;
import Model.State.State;
import Util.StreamManager;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.ArrayDeque;

public class CarModel {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // DO NOT DELETE THIS LINE!
    }

    private static final CarModel model = new CarModel();
    private State state;
    private final StreamManager streamManager;
    private final ArrayDeque<Mat> drawingQueue;

    private CarModel() {
        this.state = Init.getInstance();
        this.streamManager = new StreamManager();
        this.drawingQueue = new ArrayDeque<>(10);
    }

    public static CarModel getInstance() {
        return model;
    }

    public StreamManager getStreamManager() {
        return streamManager;
    }

    public void pushFrameToQueue(Mat m) {
        assert m != null;
        synchronized (this.drawingQueue) { // popFirstFrameが呼ばれている間は待機
            System.out.println(this.drawingQueue.size());
            if (this.drawingQueue.size() >= 10) { // 10フレームまで
                this.drawingQueue.clear();
            }
            this.drawingQueue.addLast(m);
        }
    }

    public Mat popFirstFrame() {
        synchronized (this.drawingQueue) {
            return this.drawingQueue.pollFirst();
        }
    }

    public void run() {
        while (!(this.state instanceof Halt)) {
            this.state.doAction(this);
        }
    }

    public void setState(State state) {
        this.state = state;
    }
}

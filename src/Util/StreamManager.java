package Util;

import org.intel.rs.frame.FrameList;
import org.intel.rs.pipeline.Config;
import org.intel.rs.pipeline.Pipeline;
import org.intel.rs.types.Format;
import org.intel.rs.types.Stream;

public class StreamManager {
    final Pipeline pipeline;

    public StreamManager() {
        this.pipeline = new Pipeline();
        final var config = new Config();
        config.enableStream(Stream.Color, 424, 240, Format.Bgr8, 30);
        config.enableStream(Stream.Depth, 424, 240, Format.Z16, 30);
        //config.enableStream(Stream.Infrared, 1, 424, 240, Format.Y8, 30);
        //config.enableStream(Stream.Infrared, 2, 424, 240, Format.Y8, 30);
        pipeline.start(config);
    }

    public StreamManager(final String configPath) {
        this.pipeline = new Pipeline(); // TODO: not implemented!
    }

    public FrameList getFrameList() {
        return pipeline.waitForFrames();
    }
}

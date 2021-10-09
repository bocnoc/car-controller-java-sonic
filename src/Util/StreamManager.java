package Util;

import org.intel.rs.frame.FrameList;
import org.intel.rs.pipeline.Config;
import org.intel.rs.pipeline.Pipeline;
import org.intel.rs.types.Format;
import org.intel.rs.types.Stream;

import java.util.Properties;

public class StreamManager {
    final Pipeline pipeline;

    public StreamManager(Properties properties) {
        final int width = Integer.parseInt(properties.getProperty("streamWidth", "424"));
        final int height = Integer.parseInt(properties.getProperty("streamHeight", "240"));
        final int fps = Integer.parseInt(properties.getProperty("streamFPS", "30"));
        this.pipeline = new Pipeline();
        final var config = new Config();
        config.enableStream(Stream.Color, width, height, Format.Bgr8, fps);
        config.enableStream(Stream.Depth, width, height, Format.Z16, fps);
        //config.enableStream(Stream.Infrared, 1, 424, 240, Format.Y8, 30);
        //config.enableStream(Stream.Infrared, 2, 424, 240, Format.Y8, 30);
        pipeline.start(config);
    }

    public FrameList getFrameList() {
        return pipeline.waitForFrames();
    }
}

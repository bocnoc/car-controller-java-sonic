package Util;

import org.intel.rs.frame.FrameList;
import org.intel.rs.pipeline.Config;
import org.intel.rs.pipeline.Pipeline;
import org.intel.rs.types.Format;
import org.intel.rs.types.Stream;

import java.util.Properties;

/**
 * RealSenseからデータを取得するためのクラスです
 */
public class StreamManager {
    final Pipeline pipeline;

    /**
     * 入力されたプロパティに応じてストリームの設定をします
     * @param properties "streamWidth"，"streamHeight", "streamFPS"が設定されていない場合，デフォルト値を読み取ります
     */
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

    /**
     * RealSenseから読み取ったフレームをFrameListとして返します．
     */
    public FrameList getFrameList() {
        return pipeline.waitForFrames();
    }
}

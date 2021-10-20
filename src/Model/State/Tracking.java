package Model.State;

import Model.CarModel;
import Util.ArUcoMarker;
import Util.PathPlanning;
import org.intel.rs.frame.FrameList;
import org.intel.rs.processing.Align;
import org.intel.rs.types.Pixel;
import org.intel.rs.types.Stream;
import org.intel.rs.types.Vertex;
import org.intel.rs.util.Utils;
import org.opencv.core.*;
import java.util.Arrays;

public class Tracking extends State {
    private final static State state = new Tracking();

    public static State getInstance() {
        return state;
    }

    @Override
    public void doAction(CarModel model) {
        System.out.println(this);
        final var steer = model.getSteer();
        final var throttle = model.getThrottle();
        final var holeFillingFilter = new Align(Stream.Color);
        final var streamManager = model.getStreamManager();
        while (true) {
            final FrameList data;
            {
                final var rawData = streamManager.getAlignedFrameList();
                data = holeFillingFilter.process(rawData);
                rawData.release();
            }
            final var colorFrame = data.getColorFrame();
            final var depthFrame = data.getDepthFrame();
            final Mat colorMat = new Mat(colorFrame.getHeight(), colorFrame.getWidth(), CvType.CV_8UC3, colorFrame.getData());
            final ArUcoMarker marker =  PathPlanning.detectMarker(colorMat);
            final Mat map = new Mat(new Size(colorMat.cols(), colorMat.rows()), CvType.CV_8UC3, new Scalar(200, 200, 200));
            final Mat out = new Mat();
            if (marker != null && marker.corners().size() == 4) {
                final var corner = marker.corners();
                final var centerPoint = marker.center();
                final var centerDepth = depthFrame.getDistance((int) centerPoint.x, (int)centerPoint.y);
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
                    System.out.println(centerDepth);
                    if (centerDepth < 0.25) {
                        steer.setScale(0);
                        throttle.setScale(0);
                        break;
                    } else if (centerDepth < 0.8) {
                        final var scale = steer.calcScaleWithPoint(colorMat, centerPoint) - 0.3;
                        steer.setScale(scale);
                    } else {
                        if (targetScreenPoint.getI() >= 0 || targetScreenPoint.getI() < colorFrame.getWidth() ||
                                targetScreenPoint.getJ() >= 0 || targetScreenPoint.getJ() < colorFrame.getHeight()
                        ) {
                            final var scale = steer.calcScaleWithPoint(colorMat, new Point(targetScreenPoint.getI(), targetScreenPoint.getJ())) - 0.2;
                            steer.setScale(scale);
                        }
                    }
                    // ↓ y座標の情報はいらないので削除
                    final var leftPoint2D = new Point(left3D.getX() * 100 + map.cols() / 2.0, left3D.getZ() * 100);
                    final var centerPoint2D = new Point(center3D.getX() * 100 + map.cols() / 2.0, center3D.getZ() * 100);
                    final var rightPoint2D = new Point(right3D.getX() * 100 + map.cols() / 2.0, right3D.getZ() * 100);
                    final var targetPointToDraw = new Point(targetPoint.x * 100 + map.cols() / 2.0, targetPoint.y * 100);
                    PathPlanning.drawPoints(map, new Scalar(0, 0, 255), leftPoint2D, centerPoint2D, rightPoint2D, targetPointToDraw);
                    PathPlanning.drawPoints(colorMat, centerPoint, new Point(targetScreenPoint.getI(), targetScreenPoint.getJ()));
                    intrinsics.release();
                }
            }
            PathPlanning.drawPoints(map, new Point(map.cols() / 2.0, 0)); // 自分の位置を描画;
            Core.flip(map, out, 0);

            final Mat concat = new Mat();
            final var list = Arrays.asList(colorMat, out);
            Core.hconcat(list, concat);
            model.pushFrameToQueue(concat);

            map.release();
            out.release();
            colorMat.release();
            colorFrame.release();
            depthFrame.release();
            data.release();
        }
        holeFillingFilter.release();
        model.setState(Halt.getInstance());
    }

    @Override
    public String toString() {
        return "TRACKING";
    }
}
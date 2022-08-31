package Util;

import org.intel.rs.frame.DepthFrame;
import org.intel.rs.types.Vertex;
import org.opencv.aruco.Aruco;
import org.opencv.aruco.Dictionary;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import static org.opencv.imgproc.Imgproc.FILLED;

/**
 * 経路探索系のメソッドいろいろ
 */
public class PathPlanning {

    /**
     * 壁情報を描画用Matに描画します
     * @param mat 壁情報を描画するMat変数
     * @param list 壁情報の格納されたArrayList
     */
    public static void drawWall(final Mat mat, ArrayList<Wall> list) {
        for (var w: list) {
            final var right = w.rightSide();
            final var left = w.leftSide();
            Imgproc.circle(mat, right, 8, new Scalar(0, 0, 0), FILLED);
            Imgproc.circle(mat, right, 5, new Scalar(0, 0, 255), FILLED);
            Imgproc.circle(mat, left, 8, new Scalar(0, 0, 0), FILLED);
            Imgproc.circle(mat, left, 5, new Scalar(0, 0, 255), FILLED);
        }
    }

    /**
     * 入力した深度情報から，これから走行する通路の左右の壁の位置を取得します
     * @param depth RealSenseから取得した深度情報
     * @param step 壁を探索する粒度の設定．0以下が指定された場合は1に設定されます．
     * @return 通路の壁の情報を格納したArrayList
     */
    public static ArrayList<Wall> getWallOfPath(final DepthFrame depth, final double threshold, int step) {
        step = (step < 0) ? 1 : step;
        final int width = depth.getWidth();
        final int height = depth.getHeight();
        final int heightEnd = height * 2 / 3;
        final var wallArray = new ArrayList<Wall>(heightEnd);
        for (int y = 0; y < heightEnd; y += step) {
            Wall prevWall = new Wall(new Point(0, y), new Point(0, y));
            double prevBoundaryX = 0;
            int x = 0;
            while (x < width) {
                // 画像内の距離が閾値より遠い->近い部分になっている境界点を探す(通路右側の壁を探索，最初の境界点は0)
                final double distance = depth.getDistance(x, y);
                final double interval = x - prevBoundaryX;
                if (distance >= threshold && interval > prevWall.getWallInterval()) { // 境界点かつ境界点同士の幅が前より大きい
                    prevWall = new Wall(new Point(prevBoundaryX, y), new Point(x, y));
                }
                // 画像内の距離が閾値より近い->遠い部分になっている境界点を探す(通路左側の壁を探索)
                x += step;
                while (x < width && depth.getDistance(x, y) < threshold) {
                    prevBoundaryX = x;
                    x += step;
                }
            }
            wallArray.add(prevWall);
        }
        return wallArray;
    }


    /**
     *  深度画像の中心部5x5の範囲で，閾値以下の点を取得します
     * @param depthFrame RealSenseで取得した深度画像
     * @param threshold 閾値
     * @return 閾値を下回る距離の点を格納したArrayList
     */
    public static ArrayList<Point> getCenterWallPointArray(final DepthFrame depthFrame, final double threshold) {
        final int width = depthFrame.getWidth();
        final int height = depthFrame.getHeight();
        final ArrayList<Point> pointArray = new ArrayList<>();
        for (int y_i = -3 ; y_i < 2; y_i++) {
            int y = height / 2 + y_i * 30;
            for (int x_i = -2; x_i < 3; x_i++) {
                int x = width / 2 + x_i * 30;
                final var distance = depthFrame.getDistance(x, y);
                if (distance <= threshold) {
                    pointArray.add(new Point(x, y));
                }
            }
        }
        return  pointArray;
    }

    final static Dictionary markerDictionary = Aruco.getPredefinedDictionary(Aruco.DICT_6X6_250);
    /**
     * 入力されたMat形式の画像からArUcoマーカを検出します
     * @param input OpenCVのMatです
     * @return マーカが見つからなかった場合，検出中に例外が発生した場合はnullを，複数検出された場合は最初に見つかったものの情報を返します
     */
    public static ArUcoMarker detectMarker(Mat input) {
        ArUcoMarker result = null;
        final Mat markerIds = new Mat();
        final var corners = new ArrayList<Mat>();
        try { // ランダムに謎の例外を吐くので握りつぶす
            Aruco.detectMarkers(input, markerDictionary, corners, markerIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (var c: corners) {
            double xSum = 0;
            double ySum = 0;
            final var cornerArr = new ArrayList<Point>();
            for (int i = 0; i < 4; i++) {
                final var x = c.get(0, i)[0];
                final var y = c.get(0, i)[1];
                xSum += x;
                ySum += y;
                cornerArr.add(new Point(x, y));
            }
            result = new ArUcoMarker(cornerArr, new Point(xSum / 4, ySum / 4));
        }
        markerIds.release();
        corners.forEach(Mat::release);
        return result;
    }

    public static Point calcTargetPoint(Vertex left, Vertex center, Vertex right, double d) {
        /*
            マーカ上の左辺の中点A(left), 中央の点C(center), 右辺の中点B(right)の三次元座標から，d[m]だけ離れた距離の点Pの座標(y座標を除く)を得る
            ただしPはAB・CP = 0 かつ |CP| = dを満たす
            a = right.x - left.x
            b = right.z - right.z
            P(X, Z) = (-b/a * (Z - center.z)) + center.x, - a * d * (1 / (a^2 + b^2) ^ (1 / 2)) + center.z)
         */
        final double a = right.getX() - left.getX();
        final double b = right.getZ() - left.getZ();
        final double Z = - a * d * Math.sqrt(1 / (a * a + b * b)) + center.getZ();
        final double X = - b / a * (Z - center.getZ()) + center.getX();
        return new Point(X, Z);
    }

//    public static Point calcPathToMarker(Vertex left, Vertex center, Vertex right, double d){
//
//    }

    public static void drawPoints(Mat map, Scalar scalar, Point... points){
        for (final var p: points) {
            Imgproc.circle(map, p, 5, scalar);
        }
    }

    public static void drawPoints(Mat map, Point... points){
        drawPoints(map, new Scalar(255, 0, 0), points);
    }

    public static void drawPoints(Mat map, Scalar scalar, ArrayList<Point> points){
        for (final var p: points) {
            Imgproc.circle(map, p, 5, scalar);
        }
    }

    public static void drawPoints(Mat map, ArrayList<Point> points){
        drawPoints(map, new Scalar(255, 0, 0), points);
    }
}

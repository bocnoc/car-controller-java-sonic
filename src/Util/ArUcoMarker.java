package Util;

import org.opencv.core.Point;

import java.util.ArrayList;

public class ArUcoMarker {
    final ArrayList<Point> corners;
    final Point center;

    public ArUcoMarker(ArrayList<Point> corners, Point center) {
        this.corners = corners;
        this.center = center;
    }

    public Point getCenterPoint() {
        return center;
    }
}

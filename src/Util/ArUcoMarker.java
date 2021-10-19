package Util;

import org.opencv.core.Point;

import java.util.ArrayList;

public record ArUcoMarker(ArrayList<Point> corners, Point center) { }

package Util;

import org.opencv.core.Point;

public record Wall(Point leftSide, Point rightSide) {

    public double getWallInterval() {
        return this.rightSide.x - this.leftSide.x;
    }

    public Point getCenterPoint() {
        return new Point((this.rightSide.x + this.leftSide.x) / 2.0, this.rightSide.y);
    }
}

package Util;

import org.opencv.core.Point;

public class Wall {
    final Point leftSide;
    final Point rightSide;

    public Wall(final Point leftSide, final Point rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
    }

    public double getWallInterval() {
        return this.rightSide.x - this.leftSide.x;
    }

    public Point getCenterPoint() {
        return new Point((this.rightSide.x + this.leftSide.x) / 2.0 , this.rightSide.y);
    }
}

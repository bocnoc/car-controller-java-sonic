public class Main {
    public static void main(String[] args) {
        Mat m = new Mat(1000, 1000, CvType.CV_8UC1, new Scalar(245));
        HighGui.createJFrame("", HighGui.WINDOW_AUTOSIZE);
        HighGui.imshow("", m);
        HighGui.waitKey(0);
    }
}

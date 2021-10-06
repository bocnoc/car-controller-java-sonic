package View;

import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

class Canvas extends JPanel {
    Image img;

    public Canvas() {
        this.setSize(new Dimension(424 * 2, 240));
    }

    public void setImage(final Mat img) {
        final var image = new BufferedImage(img.width(), img.height(), BufferedImage.TYPE_3BYTE_BGR);
        final var buf = (DataBufferByte) image.getRaster().getDataBuffer();
        final byte[] data = buf.getData();
        img.get(0, 0, data);
        img.release();
        this.img = image;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setLocation(10, 0);
        g.drawImage(img, 0, 0, null);
        this.setSize(new Dimension(424 * 2, 240));
    }
}

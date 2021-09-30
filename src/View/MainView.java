package View;

import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class MainView extends JFrame {
    final Canvas canvas;
    public MainView() {
        this.canvas = new Canvas();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final var panel = new JPanel();
        panel.setSize(424 * 2 + 30, 300);
        panel.add(canvas);
        //this.add(canvas);
        this.setContentPane(panel);

        this.setSize(424 * 2 + 30, 300);
        this.setVisible(true);
    }

    public void setStreamImage(final Mat data) {
        this.canvas.setImage(data);
    }
 }


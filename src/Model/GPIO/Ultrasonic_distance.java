package Model.GPIO;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.InputStream;
import java.io.OutputStream;

/*This class is about to get the Ultrasonic distance information which is get from Ultrasonic2.py through socket*/

public class Ultrasonic_distance {

    static Socket s;
    static InputStream is;
    static PrintWriter pw;
    private static int Ultrasonic_distance;

    // write your code here
    public static double Sonic_distance() {
        double steer_sonic = 0;
        try {
            s = new Socket("car-jetson2", 8000);
            is = s.getInputStream();
            pw = new PrintWriter(s.getOutputStream());
            pw.write("connected to server");
            pw.flush();
            byte[] buffer = new byte[1024];
            int read;
            //while ((read = is.read(buffer)) != -1) {
                read = is.read(buffer);
                String output = new String(buffer, 0, read);
//                int Ultrasonic_distance = Integer.parseInt(output);
//                if (Ultrasonic_distance < 1000) {
//                    try {
//                        System.out.println(Ultrasonic_distance);
//                        System.out.flush();
//                    } catch (NumberFormatException nfe) {
//                        System.out.println("vailid string");
//                    }
//                }
            //}
            steer_sonic = Double.parseDouble(output);
            System.out.println(steer_sonic);
            pw.close();
            s.close();
        } catch (UnknownHostException e) {
            System.out.println("Fail");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Fail");
            e.printStackTrace();
        }
        return steer_sonic;
    }

}

package Util;

import java.io.*;
import java.util.Locale;

public class Gpio implements Closeable {
    public enum Mode {
        IN,
        OUT,
    }

    public enum Value {
        HIGH,
        LOW,
        ILL;

        public static Value getValue(String value) {
           if (value.equals("0")) {
               return LOW;
           } else if (value.equals("1")) {
               return HIGH;
           }
           return ILL;
        }
    }

    final String pinNumber;

    public Gpio(String pinNumber, Mode gpioMode) throws IOException {
        this.pinNumber = pinNumber;

        final var pathToGpio = "/sys/class/gpio/";
        // gpioを定義 echo $number > /sys/class/gpio/export
        this.writeValueToFile(pathToGpio + "export", this.pinNumber);
        // gpioのモードを設定 echo $mode > /sys/class/gpio/gpio<num>/direction
        this.writeValueToFile(pathToGpio +  "gpio" + this.pinNumber + "/direction" , gpioMode.name().toLowerCase(Locale.ROOT));
    }

    public Value read() {
        final var gpioPath = String.format("/sys/class/gpio/gpio%s/value", this.pinNumber);
        try {
            final var value = this.readValueFromFile(gpioPath);
            return Value.getValue(value); // cat /sys/class/gpio/gpio${number}/value
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Value.ILL;
    }

    public void write(Value value) throws IOException {
        final var gpioPath = String.format("/sys/class/gpio/gpio%s/value", this.pinNumber);
        this.writeValueToFile(gpioPath, value.name()); // echo $value > /sys/class/gpio/gpio${number}/value
    }

    private void writeValueToFile(final String filePath, final String value) throws IOException {
        try (final var writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)))) {
            writer.write(value);
        }
    }

    private String readValueFromFile(final String filePath) throws IOException {
        try (final var reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)))) {
            return reader.readLine(); // cat $filePath
        }
    }

    @Override
    public void close() throws IOException {
        final var path = "/sys/class/gpio/unexport/";
        this.writeValueToFile(path, this.pinNumber);
    }
}

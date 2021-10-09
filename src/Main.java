import Controller.MainController;
import Model.CarModel;
import Util.Option;
import Util.OptionParser;
import View.MainView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Main {

    public static OptionParser buildOptionParser() {
        final var opParser =  new OptionParser();
        opParser.addOption(new Option("-g", "--with-gui", "-g, --with-gui: enable gui", false));
        opParser.addOption(new Option("-f", "--file", "%s FILE, %s FILE: load config from FILE", true));
        return opParser;
    }

    public static void main(String[] args) {
        final var opParser = buildOptionParser();
        opParser.parseArgs(args);
        final var properties = new Properties();
        if (opParser.getOption("-f") != null) {
            try (final var fileStream = new FileInputStream(opParser.getOption("-f").getValue())) {
                properties.load(fileStream);
                properties.list(System.out);
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
                System.out.println("Default config will be loaded");
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        final var model = new CarModel(properties);
        if (opParser.getOption("-g") != null) {
            final var view = new MainView();
            new MainController(view, model);
        }
        model.run();
        System.exit(0);

    }
}

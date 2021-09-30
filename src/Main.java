import Controller.MainController;
import Model.CarModel;
import View.MainView;

public class Main {
    public static void main(String[] args) {
        final var view = new MainView();
        final var model = CarModel.getInstance();
        new MainController(view, model);
        model.run();
        System.exit(0);
    }
}

package Controller;

import Model.CarModel;
import View.MainView;

public class MainController {
    final MainView view;
    final CarModel model;

    public MainController(MainView view, CarModel model) {
        this.view = view;
        this.model = model;
        final var screenCtrl = new ScreenController(this.view, this.model);
        screenCtrl.run();
    }
}

package Controller;

import Model.CarModel;
import View.MainView;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ScreenController {
    final MainView view;
    final CarModel model;

    public ScreenController(MainView view, CarModel model) {
        this.view = view;
        this.model = model;
    }

    public void run() {
        final var service =  Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(this::updateScreen, 0, 30, TimeUnit.MILLISECONDS);
    }

    private void updateScreen() {
        Optional.ofNullable(this.model.popFirstFrame())
                .ifPresent((this.view::setStreamImage));
    }
}

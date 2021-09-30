package Model.State;

import Model.CarModel;

public class Halt extends State {
    private static final State state = new Halt();

    public static State getInstance() {
        return state;
    }

    @Override
    public void doAction(CarModel model) {}
}
